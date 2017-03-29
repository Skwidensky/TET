package tet.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tet.CalculatedGazePacket;
import tet.RawGazePacket;
import tet.CalculatedGazePacket.CalculatedGazeDataBldr;

/**
 * {@link CalculatedGazePacket} utility
 * 
 * @author Charles
 *
 */
public class CgpUtil {

	private static double mInterval;

	/********************************************
	 * LOOKING FOR HOW A DATA POINT WAS CALCULATED? <br>
	 * YOU'LL FIND IT IN THIS METHOD, I BETCHA
	 * pRgps - a list of RawGazePackets
	 * pInterval - the collection rate (in seconds) -> 1 / SAMPLE_RATE
	 *******************************************/
	public static CalculatedGazePacket getCgpFor(ArrayList<RawGazePacket> pRgps, double pInterval) {
		mInterval = pInterval;
		ArrayList<Double> betweenFixations = new ArrayList<>();
		ArrayList<Double> fixationLengths = new ArrayList<>();

		/*
		 * Because a session could be broken up into different chunks of time
		 * (Start-Stop-Start), getting the total time via (Tn - T0) won't be
		 * accurate. Loop the whole thing and tally up the time. If a delta-T is
		 * greater than our interval, just set it to the interval
		 */
		long t1 = 0, t2 = 0;
		double totalTime = 0, totalFixations = 0, FPM = 0, percentTimeFixated = 0, timesBetweenFixations = 0,
				fixationLength = 0, totalSaccadeTime = 0, totalSmoothTrackingTime = 0, totalSaccadeDistance = 0,
				totalSmoothTrackDistance = 0, avgSaccadeSpeed = 0, avgSmoothTrackingSpeed = 0, fidgetLp = 0,
				fidgetRp = 0, fidgetAvg = 0, x1 = 0, x2 = 0, y1 = 0, y2 = 0, lP1 = 0, lP2 = 0, rP1 = 0, rP2 = 0;
		double avgPeakSaccadeAccel = getAvgPeakAcceleration(getAllSaccades(pRgps));

		int index = 0;
		int blinks = countBlinks(pRgps);
		int totalCycles = pRgps.size();

		/* Where the magic happens. Loop the entire list of RawGazePackets and do some math. */
		for (RawGazePacket rgp : pRgps) {
			if (index == 0) {
				t1 = rgp.getTimestamp();
			} else {
				t2 = rgp.getTimestamp();
				double incrementTime = (double) Math.abs(t2 - t1) / 1000;
				incrementTime = incrementTime > pInterval ? pInterval : incrementTime;
				totalTime += incrementTime;
				t1 = t2;
			}
			double distance = 0;
			if (index < pRgps.size()) {
				boolean fixated = pRgps.get(index).isFixated();
				// Keeps track of all the times between fixations
				if (!fixated) {
					timesBetweenFixations += pInterval;
					if (fixationLength > 0) {
						fixationLengths.add(fixationLength);
						fixationLength = 0;
					}
				} else if (timesBetweenFixations > 0) {
					betweenFixations.add(timesBetweenFixations);
					timesBetweenFixations = 0;
				} else {
					fixationLength += pInterval;
				}

				// Points for calculating distance
				x2 = rgp.getGazeX();
				y2 = rgp.getGazeY();

				// Pupil sizes for calculating fidgetiness
				lP2 = rgp.getPupilL();
				rP2 = rgp.getPupilR();

				// Can't calculate distance from the first index
				if (index != 0) {
					// Gaze (x1,y1) (x2,y2) delta
					distance = Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
				}
				
				if (fixated) {
					totalFixations++;
					// Smooth tracking distance
					if (distance > 1.5) {
						/*
						 * 1.5 is a magic number. The sensor itself has what I
						 * call "pixel shudder", meaning even when you're
						 * fixated, the (x,y) gaze coordinates still shift
						 * every-so-slightly. After multiple observations I
						 * found that the average "shudder" was between 1 and
						 * 1.41 pixels. Therefore, this conditional is meant to
						 * mitigate the effect of that on the actual distance
						 * traversed in smooth tracking.
						 */
						totalSmoothTrackDistance += distance;
					}
				} else {
					// Saccadic movements
					if (!fixated) {
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
			avgTimeBetweenFixations = betweenFixations.stream().mapToDouble(Double::doubleValue).sum()
					/ betweenFixations.size();
		}

		// Find the average length of a fixation
		double avgFixationLength = 0.0;
		if (fixationLengths.size() > 0) {
			for (double d : fixationLengths) {
				avgFixationLength += d;
			}
			avgFixationLength = (avgFixationLength / fixationLengths.size());
		}

		int actualNumFixations = findActualNumberOfFixations(pRgps);
		FPM = (actualNumFixations / totalTime) * 60;

		percentTimeFixated = (totalFixations * pInterval) / totalTime;
		// Time spent saccading
		totalSaccadeTime = (pRgps.size() - totalFixations) * pInterval;
		// Time spent fixated/smooth tracking
		totalSmoothTrackingTime = totalFixations * pInterval;
		// With the time spent and distance, calculate the average speeds of a
		// saccade and smooth traversal (pixels/millisecond)
		avgSaccadeSpeed = (totalSaccadeDistance / totalSaccadeTime) / 1000;
		avgSmoothTrackingSpeed = (totalSmoothTrackDistance / totalSmoothTrackingTime) / 1000;

		fidgetLp = fidgetLp / totalCycles;
		fidgetRp = fidgetRp / totalCycles;
		fidgetAvg = fidgetAvg / totalCycles;

		CalculatedGazePacket cGp = new CalculatedGazeDataBldr().withTotalTime(totalTime).withFixationsPerMin(FPM)
				.withTimeBtwnFixations(avgTimeBetweenFixations).withAvgFixationLength(avgFixationLength)
				.withSmoothDist(totalSmoothTrackDistance).withPercentTimeFixated(percentTimeFixated)
				.withAvgSaccadeSpeed(avgSaccadeSpeed).withAvgPeakSaccadeAccel(avgPeakSaccadeAccel)
				.withAvgSmoothSpeed(avgSmoothTrackingSpeed).withFidgetL(fidgetLp).withFidgetR(fidgetRp)
				.withAvgFidget(fidgetAvg).withTotalFixations(actualNumFixations).withBlinks(blinks).build();

		return cGp;
	}

	/*
	 * Count and remove "blink" instances from rawFigures I count series of
	 * empty data of length 1-15 as blink instances. Anything more is likely to
	 * be caused by me shifting out of the vision zone slightly
	 */
	private static Integer countBlinks(ArrayList<RawGazePacket> pRawList) {
		int blinks = 0, series = 0, index = 0;
		ArrayList<Integer> zeroedIndices = new ArrayList<>();
		boolean blinked = false, endOfRun = false, actualBlink = true;
		for (RawGazePacket rgp : pRawList) {
			double x = rgp.getGazeX();
			double y = rgp.getGazeY();
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
						boolean fakeBlink = pRawList.get(i).getGazeX() == 0.0 && pRawList.get(i).getGazeY() == 0.0;
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

			if (series < 2 || series > 15) {
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
		}

		return blinks;
	}

	// I count 3 "true" fixation booleans in a row a verified fixation
	private static Integer findActualNumberOfFixations(ArrayList<RawGazePacket> pList) {
		int actualNumberOfFixations = 0, count = 0;
		for (RawGazePacket rgp : pList) {
			if (count == 3) {
				actualNumberOfFixations++;
			}

			if (rgp.isFixated()) {
				count++;
			} else {
				count = 0;
			}
		}
		return actualNumberOfFixations;
	}

	/**
	 * acceleration = (delta-v / time)
	 * 
	 * @param pSaccadeList
	 *            -- list of lists containing each saccadic instance in the
	 *            session
	 * @return the average acceleration of the subject's saccadic movement in
	 *         (pixels/s^2)
	 */
	private static double getAvgPeakAcceleration(ArrayList<ArrayList<RawGazePacket>> pSaccadeList) {
		ArrayList<Double> peaks = new ArrayList<>();
		for (ArrayList<RawGazePacket> saccade : pSaccadeList) {
			int idx = 0;
			double acc = 0, v1 = 0, v2 = 0, x1 = 0, y1 = 0, x2 = 0, y2 = 0;
			for (RawGazePacket rgp : saccade) {
				x1 = rgp.getGazeX();
				y1 = rgp.getGazeY();
				if (idx != 0) {
					// gaze (x1,y1) (x2,y2) delta
					double distance = Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
					// final velocity -- distance / time
					v2 = (distance / mInterval) / 1000;
					double tmpAcc = Math.abs((v2 - v1) / mInterval);
					// if this acceleration was greater than the previous one,
					// it is the new peak
					if (tmpAcc > acc) {
						acc = tmpAcc;
					}
				}

				// initial velocity rebase
				v1 = v2;
				// initial gaze point rebase
				x2 = x1;
				y2 = y1;
				idx++;
			}
			peaks.add(acc);
		}
		// return the average peak acceleration for a saccade
		return peaks.stream().mapToDouble(a -> a).average().getAsDouble();
	}

	/**
	 * Extract only saccadic movement -- for use in calculated average
	 * acceleration I count 10 consecutive "false" fixation booleans a saccade,
	 * so long as there are no empty values in the series (i.e. x = 0, y = 0)
	 * Also, TET's saccade calculations include a latency period after the
	 * saccade has taken place and before the sensor recognizes that you're
	 * fixated again. This short buffer between actual saccadic movement and the
	 * next fixation is a sort of "settling in" period that should be ignored.
	 * It normally accounts for the last 50% of a saccade's non-fixated time, so just
	 * chop off that 50%.
	 * 
	 * @param pRgpList
	 *            the full list of raw gaze data
	 * @return a list of lists -- each inner list is a saccade
	 */
	private static ArrayList<ArrayList<RawGazePacket>> getAllSaccades(ArrayList<RawGazePacket> pRgpList) {
		boolean validSeries = true;
		ArrayList<ArrayList<RawGazePacket>> saccadeInstances = new ArrayList<>();
		ArrayList<RawGazePacket> singleSaccade = new ArrayList<>();

		// collect all non-fixated series as ArrayLists
		for (RawGazePacket rgp : pRgpList) {
			if (!rgp.isFixated()) {
				singleSaccade.add(rgp);
			} else {
				int seriesSize = singleSaccade.size();
				// if there are more than 15 instances in this series
				if (seriesSize > 15) {
					// and there are no invalid packets in this series
					for (RawGazePacket rgp2 : singleSaccade) {
						if (rgp2.getGazeX() == 0.0 && rgp2.getGazeY() == 0.0) {
							validSeries = false;
						}
					}
					// prune the 50% "settling in" instances and keep the series
					if (validSeries) {
						for (int i = seriesSize - 1; i > seriesSize / 2; i--) {
							singleSaccade.remove(i);
						}
						saccadeInstances.add(new ArrayList<>(singleSaccade));
					}
					validSeries = true;
				}
				// clear the series for the next go 'round
				singleSaccade.clear();
			}
		}
		return saccadeInstances;
	}

}
