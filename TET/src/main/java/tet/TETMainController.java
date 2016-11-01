package tet;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.theeyetribe.clientsdk.GazeManager;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import tet.CalculatedGazePacket.CalculatedGazeDataBldr;
import tet.util.ExportUtil;

public class TETMainController {
	private static final Logger sLog = LoggerFactory.getLogger(TETMainController.class);

	private final Timeline mPeriodicCalculation = new Timeline(
			new KeyFrame(Duration.seconds(5), new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					sLog.info("Updating GUI labels");
					calculateTheGoodStuff(false);
				}
			}));

	// Data structures
	private volatile ArrayList<double[]> mRawFigures = new ArrayList<>();
	private volatile ArrayList<Boolean> mFixationsVsaccades = new ArrayList<>();
	private volatile ArrayList<String> mRawStringOutput = new ArrayList<>();
	private volatile ArrayList<String> mTheGoodStuff = new ArrayList<>();

	// Output and calculation
	private final String RAW_COLUMNS = "timestamp,smoothX,smoothY,pupil_L,pupil_R,fixated";
	private final String TGS_COLUMNS = "total_time,fixations_per_minute,total_fixations,avg_time_between_fixations,avg_length_of_fixation,percent_time_fixated,avg_saccade_speed,blinks,fidget_L_eye,fidget_R_eye,fidget_B_eyes";
	private final double INTERVAL = 33.3333;
	private final double SAMPLE_RATE = 1 / INTERVAL; // 30Hz

	// Timestamp formatter
	DecimalFormat mTf = new DecimalFormat("#");
	// Number formatter
	final DecimalFormat mDf = new DecimalFormat("#.#####");

	@FXML
	HBox mMainHbox;
	@FXML
	TextArea mTextOutputArea;
	@FXML
	Button mExportBtn, mStartBtn, mStopBtn, mClearBtn;
	@FXML
	Label mTotalTimeLbl, mTotalFixationsLbl, mActualFixationsLbl, mTimeBetweenFixationsLbl, mAvgFixationLenLbl,
			mPercentTimeFixatedLbl, mBlinksLbl, mFixationsPerMinLbl, mTotalSacDistanceLbl, mTotalSacTimeLbl,
			mAvgSacSpeedLbl, mLFidgetLbl, mRFidgetLbl, mBFidgetLbl;

	@FXML
	private void initialize() {
		sLog.info("Initializing main area");

		mPeriodicCalculation.setCycleCount(Timeline.INDEFINITE);

		// Keep the text area scrolling with new data
		mTextOutputArea.textProperty().addListener(change -> mTextOutputArea.setScrollTop(Double.MAX_VALUE));
		addRawGazeData(RAW_COLUMNS + System.lineSeparator());
		mTheGoodStuff.add(TGS_COLUMNS + System.lineSeparator());

		mExportBtn.disableProperty().bind(mStartBtn.disabledProperty());
		mStartBtn.disableProperty().bind(mStopBtn.disabledProperty().not());
		mStartBtn.setOnAction(event -> {
			Platform.runLater(() -> new GazeConnection(this));
			mPeriodicCalculation.play();
			mStopBtn.setDisable(false);
		});

		mStopBtn.setOnAction(event -> {
			GazeManager.getInstance().deactivate();
			mPeriodicCalculation.stop();
			mStopBtn.setDisable(true);
		});

		mClearBtn.disableProperty().bind(mStartBtn.disabledProperty());

		// TODO: Latency button
		// TODO: Concentration quotient

	}

	/* Add a row of >Raw< Gaze Data to the list */
	private void addRawGazeData(String pGazeData) {
		mRawStringOutput.add(pGazeData);
		Platform.runLater(() -> mTextOutputArea.appendText(pGazeData));
	}

	/* Add >Calculated< Gaze Data to the list */
	private void addTheGoodStuff(CalculatedGazePacket pCgp) {
		Joiner joiner = Joiner.on(",");

		mTheGoodStuff.add(joiner.join(Arrays.asList(pCgp.getTotalTime(), mDf.format(pCgp.getFixationsPerMin()),
				pCgp.getTotalFixations(), mDf.format(pCgp.getTimeBetweenFixations()),
				mDf.format(pCgp.getFixationLength()), mDf.format(pCgp.getPercentTimeFixated()),
				mDf.format(pCgp.getAvgSaccadeSpeed()), pCgp.getBlinks(), mDf.format(pCgp.getFidgetL()),
				mDf.format(pCgp.getFidgetR()), mDf.format(pCgp.getAvgFidget()))));
	}

	/********************************************
	 * LOOKING FOR HOW A DATA POINT WAS CALCULATED? <br>
	 * YOU'LL FIND IT IN THIS METHOD, I BETCHA
	 *******************************************/
	private void calculateTheGoodStuff(boolean pExport) {
		// save off these two member structures -- they might still be updating
		// during these calculations
		ArrayList<Boolean> copyOfFixationsVSaccades = new ArrayList<>(mFixationsVsaccades);
		ArrayList<double[]> copyOfRawFigures = new ArrayList<>(mRawFigures);

		ArrayList<Double> betweenFixations = new ArrayList<>();
		ArrayList<Double> fixationLengths = new ArrayList<>();

		double totalTime = (copyOfRawFigures.get(copyOfRawFigures.size() - 1)[0] - copyOfRawFigures.get(0)[0]);
		double totalFixations = 0, FPM = 0, percentTimeFixated = 0, timesBetweenFixations = 0, fixationLength = 0,
				totalSaccadeTime = 0, totalSaccadeDistance = 0, avgSaccadeSpeed = 0, fidgetLp = 0, fidgetRp = 0,
				fidgetAvg = 0, x1 = 0, x2 = 0, y1 = 0, y2 = 0, lP1 = 0, lP2 = 0, rP1 = 0, rP2 = 0;		
		
		int index = 0;		
		int blinks = countBlinks(copyOfFixationsVSaccades, copyOfRawFigures);
		int totalCycles = copyOfRawFigures.size();
		
		for (double[] dArray : copyOfRawFigures) {
			if (index < copyOfRawFigures.size() && index < copyOfFixationsVSaccades.size()) {
				boolean fixated = copyOfFixationsVSaccades.get(index);
				// Keeps a tally of the times between fixations
				if (!fixated) {
					timesBetweenFixations += SAMPLE_RATE;
					if (fixationLength > 0) {
						fixationLengths.add(fixationLength);
						fixationLength = 0;
					}
				} else if (timesBetweenFixations > 0) {
					betweenFixations.add(timesBetweenFixations);
					timesBetweenFixations = 0;
				} else {
					fixationLength += SAMPLE_RATE;
				}

				// Points for calculating distance
				x2 = dArray[1];
				y2 = dArray[2];

				// Pupil sizes for calculating fidgetiness
				lP2 = dArray[3];
				rP2 = dArray[4];

				if (fixated) {
					totalFixations++;
				} else {
					// Can't calculate distance from the first index
					if (index != 0 && !fixated) {
						// If I'm not fixated, how far did my eyes move?
						double distance = Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
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
			for (double d : betweenFixations) {
				avgTimeBetweenFixations += d;
			}
			avgTimeBetweenFixations = (avgTimeBetweenFixations / betweenFixations.size());
		}

		// Find the average length of a fixation
		double avgFixationLength = 0.0;
		if (fixationLengths.size() > 0) {
			for (double d : fixationLengths) {
				avgFixationLength += d;
			}
			avgFixationLength = (avgFixationLength / fixationLengths.size());
		}

		int actualNumFixations = findActualNumberOfFixations();
		FPM = (actualNumFixations / (totalTime / 1000)) * 60;

		percentTimeFixated = (totalFixations * INTERVAL) / totalTime;
		// Time spent saccading
		totalSaccadeTime = (copyOfFixationsVSaccades.size() - totalFixations) * INTERVAL;
		// With the time spent and distance, calculate the average speed of a
		// saccade (pixels/millisecond)
		avgSaccadeSpeed = totalSaccadeDistance / totalSaccadeTime;

		fidgetLp = fidgetLp / totalCycles;
		fidgetRp = fidgetRp / totalCycles;
		fidgetAvg = fidgetAvg / totalCycles;

		CalculatedGazePacket cGp = new CalculatedGazeDataBldr().withTotalTime(totalTime).withFixationsPerMin(FPM)
				.withTimeBtwnFixations(avgTimeBetweenFixations).withAvgFixationLength(avgFixationLength)
				.withPercentTimeFixated(percentTimeFixated).withAvgSaccadeSpeed(avgSaccadeSpeed).withFidgetL(fidgetLp)
				.withFidgetR(fidgetRp).withAvgFidget(fidgetAvg).withTotalFixations(actualNumFixations)
				.withBlinks(blinks).build();

		if (pExport) {
			addTheGoodStuff(cGp);
		}

		mTotalTimeLbl.setText(String.valueOf(totalTime));
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
	}

	// I count 3 "true" fixation booleans in a row a verified fixation
	private Integer findActualNumberOfFixations() {
		int actualNumberOfFixations = 0, count = 0;
		for (Boolean b : mFixationsVsaccades) {
			if (count == 3) {
				actualNumberOfFixations++;
			}

			if (b) {
				count++;
			} else {
				count = 0;
			}
		}
		return actualNumberOfFixations;
	}

	/*
	 * Count and remove "blink" instances from rawFigures I count series of
	 * empty data of length 2-5 as blink instances. Anything less could simply
	 * be dropped data, and anything more is likely to be caused by me shifting
	 * out of the vision zone slightly
	 */
	private Integer countBlinks(ArrayList<Boolean> pFixVSac, ArrayList<double[]> pRawList) {
		int blinks = 0, series = 0, index = 0;
		ArrayList<Integer> zeroedIndices = new ArrayList<>();
		boolean blinked = false, endOfRun = false, actualBlink = true;
		for (double[] d : pRawList) {
			double x = d[1];
			double y = d[2];
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
						boolean fakeBlink = pRawList.get(i)[1] == 0.0 && pRawList.get(i)[2] == 0.0;
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

			if (series < 2 || series > 5) {
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
			pFixVSac.remove(indexToRemove);
		}

		return blinks;
	}

	/* Each entry is a timestamped data point */
	private void addRawFigures(double[] pFigures) {
		mRawFigures.add(pFigures);
	}

	/**
	 * Write the currently stored data to file
	 */
	public void export() {
		calculateTheGoodStuff(true);
		ExportUtil.export(mRawStringOutput, mTheGoodStuff);
		resetEverything();
	}

	/**
	 * Clear the controller's lists for a fresh start
	 */
	public void resetEverything() {
		sLog.info("Resetting all structures");
		mTextOutputArea.clear();
		mRawFigures.clear();
		mFixationsVsaccades.clear();
		mRawStringOutput.clear();
		addRawGazeData(RAW_COLUMNS + System.lineSeparator());
		mTheGoodStuff.clear();
		mTheGoodStuff.add(TGS_COLUMNS + System.lineSeparator());
	}

	/**
	 * Give the controller a fresh packet of raw TET data
	 * 
	 * @param pPacket
	 *            -- contains all gatherable parameters from TET device
	 */
	public void update(RawGazePacket pPacket) {
		double timestamp = (double) pPacket.getTimestamp();
		double gX = pPacket.getGazeX();
		double gY = pPacket.getGazeY();
		double pL = pPacket.getPupilL();
		double pR = pPacket.getPupilR();
		boolean fixed = pPacket.isFixated();
		// With what we'll calculate
		double[] rawFigures = { timestamp, gX, gY, pL, pR };
		addRawFigures(rawFigures);
		// Don't add fixation boolean if these aren't valid
		mFixationsVsaccades.add(fixed);

		// What gets pushed out to file
		String rawGazing = mTf.format(timestamp) + "," + gX + "," + gY + "," + pL + "," + pR + "," + fixed
				+ System.lineSeparator();
		addRawGazeData(rawGazing);
	}
}
