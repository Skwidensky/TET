package tet.weka;

import java.util.ArrayList;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.theeyetribe.clientsdk.GazeManager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import tet.CalculatedGazePacket;
import tet.GazeConnection;
import tet.IGazeController;
import tet.RawGazePacket;
import tet.TETMainController;
import tet.db.DatabaseMgr;
import tet.db.sSqlCmds;
import tet.util.CgpUtil;

/**
 * This class is meant to be a collection center for measured eye-gaze data. The
 * default exercise right now is reading the opening paragraph to the book
 * "Confederacy of Dunces" by John Toole
 */
public class TrainingDialogController implements IGazeController {

	private static final Logger sLog = LoggerFactory.getLogger(TrainingDialogController.class);

	private CalculatedGazePacket mCurrentTrainingCgp;
	private final ArrayList<RawGazePacket> mSingleRgpSet = new ArrayList<>();
	private double mInterval, mSampleRt;

	@FXML
	private BorderPane mParentPane;

	@FXML
	private Button mStartBtn, mStopBtn, mDoneBtn;

	@FXML
	private TextField mSqlTableName, mName;

	@FXML
	private void initialize() {
		mParentPane.setStyle("-fx-background-color: white;");
		mStartBtn.disableProperty().bind(mStopBtn.disabledProperty().not().or(mSqlTableName.textProperty().isEmpty())
				.or(mName.textProperty().isEmpty()));
		mStopBtn.setDisable(true);
	}

	@FXML
	private void start() {
		Platform.runLater(() -> {
			new GazeConnection(this);
			String frameRate = GazeManager.getInstance().getFrameRate().toString();
			mSampleRt = frameRate.equals(GazeManager.getInstance().getFrameRate().FPS_30.toString()) ? 30 : 60;
			mInterval = 1 / mSampleRt;
			mStopBtn.setDisable(false);
			sLog.info("Connected to gaze server at " + mSampleRt + "Hz");
		});
	}

	@FXML
	private void stop() {
		mStopBtn.setDisable(true);
		GazeManager.getInstance().deactivate();
		calculateTheGoodStuff();
	}

	@FXML
	private void done() {
		TETMainController.getInstance().getMainView().getChildren().remove(mParentPane);
	}

	@Override
	public void calculateTheGoodStuff() {
		CalculatedGazePacket cGp = CgpUtil.getCgpFor(mSingleRgpSet, mInterval);
		// confirmation dialog to determine whether or not to keep this
		// collected instance
		Alert keepThisInstance = new Alert(AlertType.CONFIRMATION);
		keepThisInstance.setHeaderText("Result");
		keepThisInstance.setContentText(cGp.toString());

		ButtonType keep = new ButtonType("Keep");
		ButtonType discard = new ButtonType("Discard");
		keepThisInstance.getButtonTypes().setAll(keep, discard);

		Optional<ButtonType> yeaOrNay = keepThisInstance.showAndWait();
		if (yeaOrNay.get() == keep) {
			mCurrentTrainingCgp = cGp;
			dumpRawGazeData();
			dumpGoodStuff();
		}
		mSingleRgpSet.clear();
		mCurrentTrainingCgp = CalculatedGazePacket.EMPTY_PACKET;
	}

	@Override
	public void update(RawGazePacket pRawPckt) {
		mSingleRgpSet.add(pRawPckt);
	}

	private void dumpRawGazeData() {
		StringJoiner sj = new StringJoiner(",");
		for (RawGazePacket raw : mSingleRgpSet) {
			sj.add("('" + raw.getFormattedInstance() + "')");
		}
		if (sj.toString().isEmpty()) {
			return;
		}
		String tbl = mSqlTableName.getText();
		String[] command = new String[2];
		command[0] = sSqlCmds.createRawTbl(tbl);
		command[1] = sSqlCmds.insertRawOutputStrings(tbl + "_raw", sj.toString());
		DatabaseMgr.sqlCommand(false, "", command);
	}

	private void dumpGoodStuff() {
		String tbl = mSqlTableName.getText();
		String[] command = new String[3];
		command[0] = sSqlCmds.createEyeTbl(tbl);
		command[1] = sSqlCmds.truncateTbl(tbl + "_calculated");
		command[2] = sSqlCmds.insertTrainingValue(tbl,
				"(" + mCurrentTrainingCgp.getFormattedInstance() + ",'" + mName.getText() + "')");
		DatabaseMgr.sqlCommand(false, "", command);
	}
}