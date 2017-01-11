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
import javafx.fxml.FXMLLoader;
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
import tet.util.CgpUtil;
import tet.util.HeatMapProvider;

public class TETMainController implements IGazeController {
	private static final Logger sLog = LoggerFactory.getLogger(TETMainController.class);

	private static TETMainController mInstance;
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
	Button mCalcBtn, mStartBtn, mStopBtn, mShowTablesBtn, mTrainBtn, mTestBtn;
	@FXML // output Labels
	Label mTotalTimeLbl, mActualFixationsLbl, mTimeBetweenFixationsLbl, mAvgFixationLenLbl,
			mPercentTimeFixatedLbl, mBlinksLbl, mFixationsPerMinLbl, mTotalSacDistanceLbl, mTotalSacTimeLbl,
			mAvgSacSpeedLbl, mLFidgetLbl, mRFidgetLbl, mBFidgetLbl, mSmoothTrkDistLbl, mConcQuotient;

	@FXML
	private void initialize() {
		sLog.info("Initializing main area");
		mInstance = this;
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
				mSampleRt = frameRate.equals(GazeManager.getInstance().getFrameRate().FPS_30.toString()) ? 30 : 60;
				mInterval = 1 / mSampleRt;
				sLog.info("Connected to gaze server at " + mSampleRt + "Hz");
			});
			mCalcBtn.setDisable(true);
			mStopBtn.setDisable(false);
			mShowTablesBtn.setDisable(true);
			mTrainBtn.setDisable(true);
			mTestBtn.setDisable(true);
			mMySqlTableName.setDisable(true);
		});

		mStopBtn.setOnAction(event -> {
			GazeManager.getInstance().deactivate();
			dumpRawGazeData();
			mCalcBtn.setDisable(false);
			mStopBtn.setDisable(true);
			mShowTablesBtn.setDisable(false);
			mTrainBtn.setDisable(false);
			mTestBtn.setDisable(false);
			mMySqlTableName.setDisable(false);
		});

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
	public void calculateTheGoodStuff() {
		ArrayList<RawGazePacket> rgps = getThisTestsRgpsFromStrings();
		CalculatedGazePacket cGp = CgpUtil.getCgpFor(rgps, mInterval);
		addTheGoodStuff(cGp);

		// TODO: give this more thought
		double concQuotient = (cGp.getFixationsPerMin() * cGp.getPercentTimeFixated() * cGp.getSmoothDist()) / cGp.getBlinks();

		mTotalTimeLbl.setText(mDf.format(cGp.getTotalTime()));
		mActualFixationsLbl.setText(String.valueOf(cGp.getTotalFixations()));
		mTimeBetweenFixationsLbl.setText(mDf.format(cGp.getTimeBetweenFixations()));
		mAvgFixationLenLbl.setText(mDf.format(cGp.getFixationLength()));
		mPercentTimeFixatedLbl.setText(mDf.format(cGp.getPercentTimeFixated()));
		mBlinksLbl.setText(String.valueOf(cGp.getBlinks()));
		mFixationsPerMinLbl.setText(mDf.format(cGp.getFixationsPerMin()));
		mAvgSacSpeedLbl.setText(mDf.format(cGp.getAvgSaccadeSpeed()));
		mLFidgetLbl.setText(mDf.format(cGp.getFidgetL()));
		mRFidgetLbl.setText(mDf.format(cGp.getFidgetR()));
		mBFidgetLbl.setText(mDf.format(cGp.getAvgFidget()));
		mSmoothTrkDistLbl.setText(mDf.format(cGp.getSmoothDist()));
		mConcQuotient.setText(mDf.format(concQuotient));
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
	
	public static TETMainController getInstance() {
		return mInstance;
	}
	
	public StackPane getMainView() {
		return mParentPane;
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
	
	@FXML
	private void showTrainingScreen() throws Exception {
		mParentPane.getChildren().add(FXMLLoader.load(getClass().getResource("training.fxml")));
	}
	
	@FXML
	private void showTestingScreen() {
		
	}
}
