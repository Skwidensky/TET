package tet;

import java.util.Objects;

public class CalculatedGazePacket {
	
	private final double mTotalTime;
	private final double mFixationsPerMin;
	private final double mTimeBetweenFixations;
	private final double mFixationLength;
	private final double mPercentTimeFixated;
	private final double mAvgSaccadeSpeed;
	private final double mFidgetL;
	private final double mFidgetR;
	private final double mAvgFidget;
	private final int mTotalFixations;
	private final int mBlinks;
	
	public static final class CalculatedGazeDataBldr {
		
		private double mTotalTime = 0;
		private double mFixationsPerMin = 0;
		private double mTimeBetweenFixations = 0;
		private double mFixationLength = 0;
		private double mPercentTimeFixated = 0;
		private double mAvgSaccadeSpeed = 0;
		private double mFidgetL = 0;
		private double mFidgetR = 0;
		private double mAvgFidget = 0;
		private int mTotalFixations = 0;
		private int mBlinks = 0;
		
		public CalculatedGazeDataBldr withTotalTime(double pTotalTime) {
			mTotalTime = Objects.requireNonNull(pTotalTime);
			return this;
		}
		
		public CalculatedGazeDataBldr withFixationsPerMin(double pFpm) {
			mFixationsPerMin = Objects.requireNonNull(pFpm);
			return this;
		}
		
		public CalculatedGazeDataBldr withTimeBtwnFixations(double pTime) {
			mTimeBetweenFixations = Objects.requireNonNull(pTime);
			return this;
		}
		
		public CalculatedGazeDataBldr withAvgFixationLength(double pAvgTime) {
			mFixationLength = Objects.requireNonNull(pAvgTime);
			return this;
		}
		
		public CalculatedGazeDataBldr withPercentTimeFixated(double pPctTime) {
			mPercentTimeFixated = Objects.requireNonNull(pPctTime);
			return this;
		}
		
		public CalculatedGazeDataBldr withAvgSaccadeSpeed(double pAvgSpeed) {
			mAvgSaccadeSpeed = Objects.requireNonNull(pAvgSpeed);
			return this;
		}
		
		public CalculatedGazeDataBldr withFidgetL(double pFidgetL) {
			mFidgetL = Objects.requireNonNull(pFidgetL);
			return this;
		}
		
		public CalculatedGazeDataBldr withFidgetR(double pFidgetR) {
			mFidgetR = Objects.requireNonNull(pFidgetR);
			return this;
		}
		
		public CalculatedGazeDataBldr withAvgFidget(double pAvgFidget) {
			mAvgFidget = Objects.requireNonNull(pAvgFidget);
			return this;
		}	
		
		public CalculatedGazeDataBldr withTotalFixations(int pTotalFixations) {
			mTotalFixations = Objects.requireNonNull(pTotalFixations);
			return this;
		}
		
		public CalculatedGazeDataBldr withBlinks(int pBlinks) {
			mBlinks = Objects.requireNonNull(pBlinks);
			return this;
		}
		
		public CalculatedGazePacket build() {
			return new CalculatedGazePacket(this);
		}
		
	}
	
	private CalculatedGazePacket(CalculatedGazeDataBldr pBldr) {
		mTotalTime = pBldr.mTotalTime;
		mFixationsPerMin = pBldr.mFixationsPerMin;
		mTimeBetweenFixations = pBldr.mTimeBetweenFixations;
		mFixationLength = pBldr.mFixationLength;
		mPercentTimeFixated = pBldr.mPercentTimeFixated;
		mAvgSaccadeSpeed = pBldr.mAvgSaccadeSpeed;
		mFidgetL = pBldr.mFidgetL;
		mFidgetR = pBldr.mFidgetR;
		mAvgFidget = pBldr.mAvgFidget;
		mTotalFixations = pBldr.mTotalFixations;
		mBlinks = pBldr.mBlinks;
	}

	public double getTotalTime() {
		return mTotalTime;
	}

	public double getFixationsPerMin() {
		return mFixationsPerMin;
	}

	public double getTimeBetweenFixations() {
		return mTimeBetweenFixations;
	}

	public double getFixationLength() {
		return mFixationLength;
	}

	public double getPercentTimeFixated() {
		return mPercentTimeFixated;
	}

	public double getAvgSaccadeSpeed() {
		return mAvgSaccadeSpeed;
	}

	public double getFidgetL() {
		return mFidgetL;
	}

	public double getFidgetR() {
		return mFidgetR;
	}

	public double getAvgFidget() {
		return mAvgFidget;
	}

	public int getTotalFixations() {
		return mTotalFixations;
	}

	public int getBlinks() {
		return mBlinks;
	}

	@Override
	public String toString() {
		return "CalculatedGazePacket [mTotalTime=" + mTotalTime + ", mFixationsPerMin=" + mFixationsPerMin
				+ ", mTimeBetweenFixations=" + mTimeBetweenFixations + ", mFixationLength=" + mFixationLength
				+ ", mPercentTimeFixated=" + mPercentTimeFixated + ", mAvgSaccadeSpeed=" + mAvgSaccadeSpeed
				+ ", mFidgetL=" + mFidgetL + ", mFidgetR=" + mFidgetR + ", mAvgFidget=" + mAvgFidget
				+ ", mTotalFixations=" + mTotalFixations + ", mBlinks=" + mBlinks + "]";
	}

}
