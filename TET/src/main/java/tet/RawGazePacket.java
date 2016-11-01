package tet;

import java.util.Objects;

public class RawGazePacket {
		
	private final long mTimestamp;
	private final double mGazeX;
	private final double mGazeY;
	private final double mPupilL;
	private final double mPupilR;
	private final boolean mIsFixated;
	
	public static final class RawGazeDataBldr {
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
