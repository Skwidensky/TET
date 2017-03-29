package tet;

import java.text.DecimalFormat;
import java.util.Objects;
import java.util.Random;

import com.google.common.base.Joiner;

/**
 * A data structure which consists of all the variables accessible through TET's
 * eye-tracking API -- available at 30Hz or 60Hz
 * Each of these packets represents and single Hz collection cycle from the device
 * 
 * @author Charles
 *
 */
public class RawGazePacket {

	public static RawGazePacket EMPTY_PACKET = new RawGazeDataBldr().build();

	private final long mTimestamp;
	private final double mGazeX;
	private final double mGazeY;
	private final double mPupilL;
	private final double mPupilR;
	private final boolean mIsFixated;

	public static final class RawGazeDataBldr {
		private final Random mRand = new Random();
		private long mTimestamp = 0;
		private double mGazeX = 0;
		private double mGazeY = 0;
		private double mPupilL = 0;
		private double mPupilR = 0;
		private boolean mIsFixated = false;

		public RawGazeDataBldr withTimestamp(long pTimestamp) {
			mTimestamp = Objects.requireNonNull(pTimestamp);
			return this;
		}

		public RawGazeDataBldr withGazeX(double pGazeX) {
			mGazeX = Objects.requireNonNull(pGazeX);
			return this;
		}

		public RawGazeDataBldr withGazeY(double pGazeY) {
			mGazeY = Objects.requireNonNull(pGazeY);
			return this;
		}

		public RawGazeDataBldr withPupilL(double pPupilL) {
			mPupilL = Objects.requireNonNull(pPupilL);
			return this;
		}

		public RawGazeDataBldr withPupilR(double pPupilR) {
			mPupilR = Objects.requireNonNull(pPupilR);
			return this;
		}

		public RawGazeDataBldr withFixated(boolean pIsFixated) {
			mIsFixated = Objects.requireNonNull(pIsFixated);
			return this;
		}

		public RawGazeDataBldr random() {
			mTimestamp = mRand.nextLong();
			mGazeX = mRand.nextInt(1000);
			mGazeY = mRand.nextInt(1000);
			mPupilL = mRand.nextInt(1000);
			mPupilR = mRand.nextInt(1000);
			mIsFixated = mRand.nextBoolean();
			return this;
		}

		public RawGazeDataBldr from(String pRawString) {
			String[] data = pRawString.split(",");
			mTimestamp = Long.valueOf(data[0]);
			mGazeX = Double.valueOf(data[1]);
			mGazeY = Double.valueOf(data[2]);
			mPupilL = Double.valueOf(data[3]);
			mPupilR = Double.valueOf(data[4]);
			mIsFixated = Boolean.valueOf(data[5]);
			return this;
		}

		public RawGazePacket build() {
			return new RawGazePacket(this);
		}
	}

	private RawGazePacket(RawGazeDataBldr pBldr) {
		mTimestamp = pBldr.mTimestamp;
		mGazeX = pBldr.mGazeX;
		mGazeY = pBldr.mGazeY;
		mPupilL = pBldr.mPupilL;
		mPupilR = pBldr.mPupilR;
		mIsFixated = pBldr.mIsFixated;
	}

	/**
	 * @return this cycle's timestamp
	 */
	public long getTimestamp() {
		return mTimestamp;
	}

	/**
	 * @return this cycle's X gaze coordinate
	 */
	public double getGazeX() {
		return mGazeX;
	}

	/**
	 * @return this cycle's Y gaze coordinate
	 */
	public double getGazeY() {
		return mGazeY;
	}

	/**
	 * @return this cycle's left pupil size
	 */
	public double getPupilL() {
		return mPupilL;
	}

	/**
	 * @return this cycle's right pupil size
	 */
	public double getPupilR() {
		return mPupilR;
	}

	/**
	 * @return !IMPORTANT! whether or not the user was fixated during this cycle
	 */
	public boolean isFixated() {
		return mIsFixated;
	}

	/**
	 * @return a comma delimited string of this packet's variables
	 */
	public String getFormattedInstance() {
		DecimalFormat df = new DecimalFormat("#");
		Joiner joiner = Joiner.on(",");
		return joiner.join(df.format(mTimestamp), df.format(mGazeX), df.format(mGazeY), df.format(mPupilL),
				df.format(mPupilR), mIsFixated);
	}

	@Override
	public String toString() {
		return "GazeData [mTimestamp=" + mTimestamp + ", mGazeX=" + mGazeX + ", mGazeY=" + mGazeY + ", mPupilL="
				+ mPupilL + ", mPupilR=" + mPupilR + ", mIsFixated=" + mIsFixated + "]";
	}
}
