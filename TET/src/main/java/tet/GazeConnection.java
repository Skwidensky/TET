package tet;

import com.theeyetribe.clientsdk.GazeManager;
import com.theeyetribe.clientsdk.IGazeListener;
import com.theeyetribe.clientsdk.data.GazeData;

import tet.RawGazePacket.RawGazeDataBldr;

public class GazeConnection implements IGazeListener {
	TETMainController mCont;

	// Connect to TET's server for collection of the actual data
	public GazeConnection(TETMainController pCont) {
		mCont = pCont;
		GazeManager.getInstance().activate();
		GazeManager.getInstance().addGazeListener(this);
	}

	@Override
	public void onGazeUpdate(GazeData pGazeData) {
		long timestamp = pGazeData.timeStamp;
		double gX = pGazeData.smoothedCoordinates.x;
		double gY = pGazeData.smoothedCoordinates.y;
		double pL = pGazeData.leftEye.pupilSize;
		double pR = pGazeData.rightEye.pupilSize;
		boolean fixed = pGazeData.isFixated;

		RawGazePacket gazeData = new RawGazeDataBldr().withTimestamp(timestamp).withGazeX(gX).withGazeY(gY).withPupilL(pL)
				.withPupilR(pR).withFixated(fixed).build();		

		if (gX >= 0 && gY >= 0) {
			mCont.update(gazeData);
		}
	}

}
