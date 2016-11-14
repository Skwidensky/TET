package tet;

import java.util.Objects;
import java.util.Random;

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

	public long getTimestamp() {
		return mTimestamp;
	}

	public double getGazeX() {
		return mGazeX;
	}

	public double getGazeY() {
		return mGazeY;
	}

	public double getPupilL() {
		return mPupilL;
	}

	public double getPupilR() {
		return mPupilR;
	}

	public boolean isFixated() {
		return mIsFixated;
	}

	@Override
	public String toString() {
		return "GazeData [mTimestamp=" + mTimestamp + ", mGazeX=" + mGazeX + ", mGazeY=" + mGazeY + ", mPupilL="
				+ mPupilL + ", mPupilR=" + mPupilR + ", mIsFixated=" + mIsFixated + "]";
	}
}
