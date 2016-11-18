package tet;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.theeyetribe.clientsdk.GazeManager;

import eu.hansolo.fx.heatmap.HeatMap;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Window;
import tet.CalculatedGazePacket.CalculatedGazeDataBldr;
import tet.RawGazePacket.RawGazeDataBldr;
import tet.db.DatabaseMgr;
import tet.db.DbTablesDialog;
import tet.db.sSqlCmds;
import tet.util.HeatMapProvider;

public class TETMainController {
	private static final Logger sLog = LoggerFactory.getLogger(TETMainController.class);

	// Data structures
	private ArrayList<String> mRawStringOutput = new ArrayList<>();
	private ArrayList<String> mTheGoodStuff = new ArrayList<>();

	// Output and calculation
	private final Executor mExec = Executors.newSingleThreadExecutor();
	private double mInterval, mSampleRt;
	private TextArea mTextOutputArea;

	// Generic formatter
	DecimalFormat mGf = new DecimalFormat("#");
	// Number formatter
	final DecimalFormat mDf = new DecimalFormat("#.####");

	@FXML
	StackPane mParentPane;
	@FXML
	BorderPane mTxtAreaBorderPane;
	@FXML
	TextField mMySqlTableName;
	@FXML
	Button mCalcBtn, mStartBtn, mStopBtn, mShowTablesBtn;
	@FXML // output Labels
	Label mTotalTimeLbl, mTotalFixationsLbl, mActualFixationsLbl, mTimeBetweenFixationsLbl, mAvgFixationLenLbl,
			mPercentTimeFixatedLbl, mBlinksLbl, mFixationsPerMinLbl, mTotalSacDistanceLbl, mTotalSacTimeLbl,
			mAvgSacSpeedLbl, mLFidgetLbl, mRFidgetLbl, mBFidgetLbl, mSmoothTrkDistLbl, mConcQuotient;

	@FXML
	private void initialize() {
		sLog.info("Initializing main area");
		initTextArea();
		initButtons();

		sLog.info("Initializing database");
		DatabaseMgr.getDbMgr();
	}

	private void initTextArea() {
		// limit the TextArea to 30 rows for the UI thread's sake
		mTextOutputArea = new TextArea() {
			@Override
			public void replaceText(int pStart, int pEnd, String pText) {
				super.replaceText(pStart, pEnd, pText);
				while (getText().split("\n", -1).length > 30) {
					int fle = getText().indexOf("\n");
					super.replaceText(0, fle + 1, "");
				}
				positionCaret(getText().length());
			}
		};

		mTextOutputArea.setEditable(false);
		mTxtAreaBorderPane.setCenter(mTextOutputArea);
	}

	@SuppressWarnings("static-access")
	private void initButtons() {
		mStartBtn.disableProperty()
				.bind(mStopBtn.disabledProperty().not().or(mMySqlTableName.textProperty().isEmpty()));
		mStartBtn.setOnAction(event -> {
			Platform.runLater(() -> {
				new GazeConnection(this);
				String frameRate = GazeManager.getInstance().getFrameRate().toString();
				mSampleRt = frameRate.equals(GazeManager.getInstance().getFrameRate().FPS_30.toString()) ? 0.03 : 0.06;
				mInterval = 1 / mSampleRt;
				sLog.info("Connected to gaze server at " + mSampleRt + "Hz");
			});
			mCalcBtn.setDisable(true);
			mStopBtn.setDisable(false);
			mShowTablesBtn.setDisable(true);
			mMySqlTableName.setDisable(true);
		});

		mStopBtn.setOnAction(event -> {
			GazeManager.getInstance().deactivate();
			dumpRawGazeData();
			mCalcBtn.setDisable(false);
			mStopBtn.setDisable(true);
			mShowTablesBtn.setDisable(false);
			mMySqlTableName.setDisable(false);
		});

		// TODO: Latency button
	}

	/* Add a row of >Raw< Gaze Data to the list */
	private void addRawGazeData(String pGazeData) {
		mRawStringOutput.add(pGazeData);
		if (mRawStringOutput.size() > 300) {
			dumpRawGazeData();
		}
		String easierToReadRaw = pGazeData.replaceAll(",", " --- ");
		Platform.runLater(() -> mTextOutputArea.appendText(easierToReadRaw + System.lineSeparator()));
	}

	private void dumpRawGazeData() {
		mExec.execute(() -> {
			ArrayList<String> tmpRaw = new ArrayList<>(mRawStringOutput);
			StringJoiner sj = new StringJoiner(",");
			for (String raw : tmpRaw) {
				sj.add("('" + raw + "')");
			}
			if (sj.toString().isEmpty()) {
				return;
			}
			String tbl = mMySqlTableName.getText();
			String[] command = new String[2];
			command[0] = sSqlCmds.createRawTbl(tbl);
			command[1] = sSqlCmds.insertRawOutputStrings(tbl + "_raw", sj.toString());
			DatabaseMgr.sqlCommand(false, "", command);
			mRawStringOutput.clear();
		});
	}

	private void dumpGoodStuff() {
		mExec.execute(() -> {
			String tbl = mMySqlTableName.getText();
			String[] command = new String[3];
			command[0] = sSqlCmds.createEyeTbl(tbl);
			command[1] = sSqlCmds.truncateTbl(tbl + "_calculated");
			command[2] = sSqlCmds.insertEyeValues(tbl, "(" + mTheGoodStuff.get(0) + ")");
			DatabaseMgr.sqlCommand(false, "", command);
			mTheGoodStuff.clear();
		});
	}

	/* Add >Calculated< Gaze Data to the list */
	private void addTheGoodStuff(CalculatedGazePacket pCgp) {
		Joiner joiner = Joiner.on(",");

		mTheGoodStuff.add(joiner.join(
				Arrays.asList(pCgp.getTotalTime(), mDf.format(pCgp.getFixationsPerMin()), pCgp.getTotalFixations(),
						mDf.format(pCgp.getTimeBetweenFixations()), mDf.format(pCgp.getFixationLength()),
						mDf.format(pCgp.getSmoothDist()), mDf.format(pCgp.getPercentTimeFixated()),
						mDf.format(pCgp.getAvgSaccadeSpeed()), mDf.format(pCgp.getFidgetL()),
						mDf.format(pCgp.getFidgetR()), mDf.format(pCgp.getAvgFidget()), pCgp.getBlinks())));
	}

	/********************************************
	 * LOOKING FOR HOW A DATA POINT WAS CALCULATED? <br>
	 * YOU'LL FIND IT IN THIS METHOD, I BETCHA
	 *******************************************/
	private void calculateTheGoodStuff() {
		ArrayList<RawGazePacket> rgps = getThisTestsRgpsFromStrings();

		ArrayList<Double> betweenFixations = new ArrayList<>();
		ArrayList<Double> fixationLengths = new ArrayList<>();

		/*
		 * Because a session could be broken up into different chunks of time
		 * (Start-Stop-Start), getting the total time via (Tn - T0) won't be
		 * accurate. Loop the whole thing and tally up the time. If a block of
		 * time is greater than our interval, just set it to the interval
		 */
		long t1 = 0, t2 = 0;
		double totalTime = 0, totalFixations = 0, FPM = 0, percentTimeFixated = 0, timesBetweenFixations = 0,
				fixationLength = 0, totalSaccadeTime = 0, totalSaccadeDistance = 0, totalSmoothTrackDistance = 0,
				avgSaccadeSpeed = 0, fidgetLp = 0, fidgetRp = 0, fidgetAvg = 0, x1 = 0, x2 = 0, y1 = 0, y2 = 0, lP1 = 0,
				lP2 = 0, rP1 = 0, rP2 = 0;

		int index = 0;
		int blinks = countBlinks(rgps);
		int totalCycles = rgps.size();

		for (RawGazePacket rgp : rgps) {
			if (index == 0) {
				t1 = rgp.getTimestamp();
			} else {
				t2 = rgp.getTimestamp();
				double incrementTime = t2 - t1;
				incrementTime = incrementTime > mSampleRt ? mSampleRt : incrementTime;
				totalTime += incrementTime;
				t1 = t2;
			}
			double distance = 0;
			if (index < rgps.size()) {
				boolean fixated = rgps.get(index).isFixated();
				// Keeps track of all the times between fixations
				if (!fixated) {
					timesBetweenFixations += mSampleRt;
					if (fixationLength > 0) {
						fixationLengths.add(fixationLength);
						fixationLength = 0;
					}
				} else if (timesBetweenFixations > 0) {
					betweenFixations.add(timesBetweenFixations);
					timesBetweenFixations = 0;
				} else {
					fixationLength += mSampleRt;
				}

				// Points for calculating distance
				x2 = rgp.getGazeX();
				y2 = rgp.getGazeY();

				// Pupil sizes for calculating fidgetiness
				lP2 = rgp.getPupilL();
				rP2 = rgp.getPupilR();

				// Can't calculate distance from the first index
				if (index != 0) {
					// Gaze (x1,y1) (x2,y2) delta
					distance = Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
				}
				if (fixated) {
					totalFixations++;
					// Smooth tracking distance
					totalSmoothTrackDistance += distance;
				} else {
					// Saccadic movements
					if (!fixated) {
						totalSaccadeDistance += distance;
					}
				}

				if (index != 0) {
					double deltalP = Math.abs(lP2 - lP1);
					double deltarP = Math.abs(rP2 - rP1);
					fidgetLp += deltalP;
					fidgetRp += deltarP;
					fidgetAvg += (deltalP + deltarP);
				}

				x1 = x2;
				y1 = y2;
				lP1 = lP2;
				rP1 = rP2;
				index++;
			}
		}

		// Find the average time between fixations
		double avgTimeBetweenFixations = 0.0;
		if (betweenFixations.size() > 0) {
			avgTimeBetweenFixations = betweenFixations.stream().mapToDouble(Double::doubleValue).sum()
					/ betweenFixations.size();
		}

		// Find the average length of a fixation
		double avgFixationLength = 0.0;
		if (fixationLengths.size() > 0) {
			for (double d : fixationLengths) {
				avgFixationLength += d;
			}
			avgFixationLength = (avgFixationLength / fixationLengths.size());
		}

		int actualNumFixations = findActualNumberOfFixations(rgps);
		FPM = (actualNumFixations / totalTime) * 60;

		percentTimeFixated = (totalFixations * mInterval / 1000) / totalTime;
		// Time spent saccading
		totalSaccadeTime = (rgps.size() - totalFixations) * mInterval;
		// With the time spent and distance, calculate the average speed of a
		// saccade (pixels/millisecond)
		avgSaccadeSpeed = totalSaccadeDistance / totalSaccadeTime;

		fidgetLp = fidgetLp / totalCycles;
		fidgetRp = fidgetRp / totalCycles;
		fidgetAvg = fidgetAvg / totalCycles;

		CalculatedGazePacket cGp = new CalculatedGazeDataBldr().withTotalTime(totalTime).withFixationsPerMin(FPM)
				.withTotalFixations(totalFixations).withTimeBtwnFixations(avgTimeBetweenFixations)
				.withAvgFixationLength(avgFixationLength).withSmoothDist(totalSmoothTrackDistance)
				.withPercentTimeFixated(percentTimeFixated).withAvgSaccadeSpeed(avgSaccadeSpeed).withFidgetL(fidgetLp)
				.withFidgetR(fidgetRp).withAvgFidget(fidgetAvg).withTotalFixations(actualNumFixations)
				.withBlinks(blinks).build();

		addTheGoodStuff(cGp);

		// TODO: give this more thought
		double concQuotient = (FPM * percentTimeFixated * totalSmoothTrackDistance) / blinks;

		mTotalTimeLbl.setText(mDf.format(totalTime));
		mTotalFixationsLbl.setText(String.valueOf(totalFixations));
		mActualFixationsLbl.setText(String.valueOf(actualNumFixations));
		mTimeBetweenFixationsLbl.setText(mDf.format(avgTimeBetweenFixations));
		mAvgFixationLenLbl.setText(mDf.format(avgFixationLength));
		mPercentTimeFixatedLbl.setText(mDf.format(percentTimeFixated));
		mBlinksLbl.setText(String.valueOf(blinks));
		mFixationsPerMinLbl.setText(mDf.format(FPM));
		mTotalSacDistanceLbl.setText(mDf.format(totalSaccadeDistance));
		mTotalSacTimeLbl.setText(mDf.format(totalSaccadeTime));
		mAvgSacSpeedLbl.setText(mDf.format(avgSaccadeSpeed));
		mLFidgetLbl.setText(mDf.format(fidgetLp));
		mRFidgetLbl.setText(mDf.format(fidgetRp));
		mBFidgetLbl.setText(mDf.format(fidgetAvg));
		mSmoothTrkDistLbl.setText(mDf.format(totalSmoothTrackDistance));
		mConcQuotient.setText(mDf.format(concQuotient));
	}

	// I count 3 "true" fixation booleans in a row a verified fixation
	private Integer findActualNumberOfFixations(ArrayList<RawGazePacket> pList) {
		int actualNumberOfFixations = 0, count = 0;
		for (RawGazePacket rgp : pList) {
			if (count == 3) {
				actualNumberOfFixations++;
			}

			if (rgp.isFixated()) {
				count++;
			} else {
				count = 0;
			}
		}
		return actualNumberOfFixations;
	}

	/*
	 * Count and remove "blink" instances from rawFigures I count series of
	 * empty data of length 1-15 as blink instances. Anything more is likely to
	 * be caused by me shifting out of the vision zone slightly
	 */
	private Integer countBlinks(ArrayList<RawGazePacket> pRawList) {
		int blinks = 0, series = 0, index = 0;
		ArrayList<Integer> zeroedIndices = new ArrayList<>();
		boolean blinked = false, endOfRun = false, actualBlink = true;
		for (RawGazePacket rgp : pRawList) {
			double x = rgp.getGazeX();
			double y = rgp.getGazeY();
			boolean zeroed = (x == 0.0 && y == 0.0);

			if (!zeroed) {
				endOfRun = true;
				series = 0;
			}

			if (blinked && endOfRun) {
				// Look ahead to make sure there are no more drops in data
				// within 10 cycles of a blink. This helps filter out data
				// drops and patchy data being misinterpreted as blinks
				for (int i = index; i <= index + 10; i++) {
					if (i < pRawList.size() - 1) {
						boolean fakeBlink = pRawList.get(i).getGazeX() == 0.0 && pRawList.get(i).getGazeY() == 0.0;
						if (fakeBlink) {
							actualBlink = false;
						}
					}
				}

				if (actualBlink) {
					blinks++;
				}
				endOfRun = false;
				actualBlink = true;
			}

			if (zeroed) {
				series++;
				zeroedIndices.add(index);
				endOfRun = false;
			}

			if (series < 2 || series > 15) {
				blinked = false;
			} else {
				blinked = true;
			}

			index++;
			if (index == pRawList.size()) {
				index = index - 1;
			}
		}

		// Get rid of zeroed rows from the data
		for (int i = zeroedIndices.size() - 1; i >= 0; i--) {
			int indexToRemove = zeroedIndices.get(i);
			pRawList.remove(indexToRemove);
		}

		return blinks;
	}

	/**
	 * Give the controller a fresh packet of raw TET data
	 * 
	 * @param pRgp
	 *            -- contains all (important) gatherable parameters from TET
	 *            device
	 */
	public void update(RawGazePacket pRgp) {
		long timestamp = pRgp.getTimestamp();
		double gX = pRgp.getGazeX();
		double gY = pRgp.getGazeY();
		double pL = pRgp.getPupilL();
		double pR = pRgp.getPupilR();
		boolean fixed = pRgp.isFixated();

		// What gets output to the raw file
		String rawGazing = mGf.format(timestamp) + "," + mGf.format(gX) + "," + mGf.format(gY) + "," + mGf.format(pL)
				+ "," + mGf.format(pR) + "," + fixed;
		addRawGazeData(rawGazing);
	}

	private ArrayList<String> getThisTestsRawStrings() {
		ArrayList<String> returnList = new ArrayList<>();
		String[] command = new String[1];
		command[0] = sSqlCmds.getRaw(mMySqlTableName.getText());
		DatabaseMgr.sqlCommand(true, "raw_sample", command);
		DatabaseMgr.getQuery().stream().forEach(rawOutput -> {
			returnList.add(rawOutput);
		});
		return returnList;
	}

	private ArrayList<RawGazePacket> getThisTestsRgpsFromStrings() {
		ArrayList<RawGazePacket> returnList = new ArrayList<>();
		getThisTestsRawStrings().forEach(sample -> returnList.add(new RawGazeDataBldr().from(sample).build()));
		return returnList;
	}

	@FXML
	private void calculate() {
		mCalcBtn.setDisable(true);
		calculateTheGoodStuff();
		dumpGoodStuff();
	}

	@FXML
	private void showDbTables() {
		mParentPane.getChildren().add(new DbTablesDialog(mParentPane));
	}
}
