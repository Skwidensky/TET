package tet.weka;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.theeyetribe.clientsdk.GazeManager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import tet.CalculatedGazePacket;
import tet.GazeConnection;
import tet.IGazeController;
import tet.RawGazePacket;
import tet.TETMainController;
import tet.util.CgpUtil;

public class TrainingDialogController implements IGazeController {
	
	private static final Logger sLog = LoggerFactory.getLogger(TrainingDialogController.class);
	
	private ArrayList<CalculatedGazePacket> mCurrentTrainingSessionPkts;
	private ArrayList<RawGazePacket> mSingleRgpSet;
	private double mInterval, mSampleRt;

	@FXML
	private BorderPane mParentPane;

	@FXML
	private void initialize() {
		mParentPane.setStyle("-fx-background-color: white;");
		mCurrentTrainingSessionPkts = new ArrayList<>();
	}

	@FXML
	private void start() {
		Platform.runLater(() -> {
			new GazeConnection(this);
			String frameRate = GazeManager.getInstance().getFrameRate().toString();
			mSampleRt = frameRate.equals(GazeManager.getInstance().getFrameRate().FPS_30.toString()) ? 30 : 60;
			mInterval = 1 / mSampleRt;
			sLog.info("Connected to gaze server at " + mSampleRt + "Hz");
		});
	}

	@FXML
	private void stop() {
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
		mCurrentTrainingSessionPkts.add(cGp);
		mSingleRgpSet.clear();
	}

	@Override
	public void update(RawGazePacket pRawPckt) {	
		mSingleRgpSet.add(pRawPckt);
	}

}
