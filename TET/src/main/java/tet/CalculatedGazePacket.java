package tet;

import java.text.DecimalFormat;
import java.util.Objects;

import com.google.common.base.Joiner;

public class CalculatedGazePacket {

	public static CalculatedGazePacket EMPTY_PACKET = new CalculatedGazeDataBldr().build();
	private final DecimalFormat df = new DecimalFormat("#.####");

	private final double mTotalTime;
	private final double mFixationsPerMin;
	private final double mTimeBetweenFixations;
	private final double mFixationLength;
	private final double mSmoothDist;
	private final double mPercentTimeFixated;
	private final double mAvgSaccadeSpeed;
	private final double mAvgSaccadeAccel;
	private final double mAvgSmoothSpeed;
	private final double mFidgetL;
	private final double mFidgetR;
	private final double mAvgFidget;
	private final double mTotalFixations;
	private final int mBlinks;

	public static final class CalculatedGazeDataBldr {

		private double mTotalTime = 0;
		private double mFixationsPerMin = 0;
		private double mTimeBetweenFixations = 0;
		private double mFixationLength = 0;
		private double mSmoothDist = 0;
		private double mPercentTimeFixated = 0;
		private double mAvgSaccadeSpeed = 0;
		private double mAvgPeakSaccadeAccel = 0;
		private double mAvgSmoothSpeed = 0;
		private double mFidgetL = 0;
		private double mFidgetR = 0;
		private double mAvgFidget = 0;
		private double mTotalFixations = 0;
		private int mBlinks = 0;

		public CalculatedGazeDataBldr from(CalculatedGazePacket pCgp) {
			mTotalTime = Objects.requireNonNull(pCgp.getTotalTime());
			mFixationsPerMin = Objects.requireNonNull(pCgp.getFixationsPerMin());
			mTimeBetweenFixations = Objects.requireNonNull(pCgp.getTimeBetweenFixations());
			mFixationLength = Objects.requireNonNull(pCgp.getFixationLength());
			mSmoothDist = Objects.requireNonNull(pCgp.getSmoothDist());
			mPercentTimeFixated = Objects.requireNonNull(pCgp.getPercentTimeFixated());
			mAvgSaccadeSpeed = Objects.requireNonNull(pCgp.mAvgSaccadeSpeed);
			mAvgPeakSaccadeAccel = Objects.requireNonNull(pCgp.mAvgSaccadeAccel);
			mFidgetL = Objects.requireNonNull(pCgp.getFidgetL());
			mFidgetR = Objects.requireNonNull(pCgp.getFidgetR());
			mAvgFidget = Objects.requireNonNull(pCgp.getAvgFidget());
			mTotalFixations = Objects.requireNonNull(pCgp.getTotalFixations());
			mBlinks = Objects.requireNonNull(pCgp.getBlinks());
			return this;
		}

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

		public CalculatedGazeDataBldr withSmoothDist(double pSmoothDist) {
			mSmoothDist = Objects.requireNonNull(pSmoothDist);
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
		
		public CalculatedGazeDataBldr withAvgPeakSaccadeAccel(double pAvgPeakAccel) {
			mAvgPeakSaccadeAccel = Objects.requireNonNull(pAvgPeakAccel);
			return this;
		}

		public CalculatedGazeDataBldr withAvgSmoothSpeed(double pSmoothSpeed) {
			mAvgSmoothSpeed = Objects.requireNonNull(pSmoothSpeed);
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

		public CalculatedGazeDataBldr withTotalFixations(double pTotalFixations) {
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
		mSmoothDist = pBldr.mSmoothDist;
		mPercentTimeFixated = pBldr.mPercentTimeFixated;
		mAvgSaccadeSpeed = pBldr.mAvgSaccadeSpeed;
		mAvgSaccadeAccel = pBldr.mAvgPeakSaccadeAccel;
		mAvgSmoothSpeed = pBldr.mAvgSmoothSpeed;
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

	public double getSmoothDist() {
		return mSmoothDist;
	}

	public double getPercentTimeFixated() {
		return mPercentTimeFixated;
	}

	public double getAvgSaccadeSpeed() {
		return mAvgSaccadeSpeed;
	}

	public double getAvgSaccadeAccel() {
		return mAvgSaccadeAccel;
	}

	public double getAvgSmoothSpeed() {
		return mAvgSmoothSpeed;
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

	public double getTotalFixations() {
		return mTotalFixations;
	}

	public int getBlinks() {
		return mBlinks;
	}

	public String getFormattedInstance() {		
		Joiner joiner = Joiner.on(",");
		return joiner.join(df.format(mTotalTime), df.format(mFixationsPerMin), mTotalFixations, df.format(mTimeBetweenFixations),
				df.format(mFixationLength), df.format(mSmoothDist), df.format(mAvgSmoothSpeed),
				df.format(mPercentTimeFixated), df.format(mAvgSaccadeSpeed), df.format(mAvgSaccadeAccel),
				df.format(mFidgetL), df.format(mFidgetR), df.format(mAvgFidget), mBlinks);
	}

	@Override
	public String toString() {
		return "CalculatedGazePacket [mTotalTime=" + df.format(mTotalTime) + ", mFixationsPerMin=" + mFixationsPerMin
				+ ", mTimeBetweenFixations=" + df.format(mTimeBetweenFixations) + ", mFixationLength=" + mFixationLength
				+ ", mSmoothDist=" + mSmoothDist + ", mPercentTimeFixated=" + mPercentTimeFixated
				+ ", mAvgSaccadeSpeed=" + mAvgSaccadeSpeed + ", mAvgSaccadeAccel=" + mAvgSaccadeAccel
				+ ", mAvgSmoothSpeed=" + mAvgSmoothSpeed + ", mFidgetL=" + mFidgetL + ", mFidgetR=" + mFidgetR
				+ ", mAvgFidget=" + mAvgFidget + ", mTotalFixations=" + mTotalFixations + ", mBlinks=" + mBlinks + "]";
	}

}
