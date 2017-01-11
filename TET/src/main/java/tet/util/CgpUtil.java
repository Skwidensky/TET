package tet.util;

import java.util.ArrayList;

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
	public static CalculatedGazePacket getCgpFor(ArrayList<RawGazePacket> pRgps, double pInterval) {
		ArrayList<Double> betweenFixations = new ArrayList<>();
		ArrayList<Double> fixationLengths = new ArrayList<>();

		/*
		 * Because a session could be broken up into different chunks of time
		 * (Start-Stop-Start), getting the total time via (Tn - T0) won't be
		 * accurate. Loop the whole thing and tally up the time. If a delta-t is
		 * greater than our interval, just set it to the interval
		 */
		long t1 = 0, t2 = 0;
		double totalTime = 0, totalFixations = 0, FPM = 0, percentTimeFixated = 0, timesBetweenFixations = 0,
				fixationLength = 0, totalSaccadeTime = 0, totalSaccadeDistance = 0, totalSmoothTrackDistance = 0,
				avgSaccadeSpeed = 0, fidgetLp = 0, fidgetRp = 0, fidgetAvg = 0, x1 = 0, x2 = 0, y1 = 0, y2 = 0, lP1 = 0,
				lP2 = 0, rP1 = 0, rP2 = 0;

		int index = 0;
		int blinks = countBlinks(pRgps);
		int totalCycles = pRgps.size();

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
					totalSmoothTrackDistance += distance;
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
		// With the time spent and distance, calculate the average speed of a
		// saccade (pixels/millisecond)
		avgSaccadeSpeed = (totalSaccadeDistance / totalSaccadeTime) / 1000;

		fidgetLp = fidgetLp / totalCycles;
		fidgetRp = fidgetRp / totalCycles;
		fidgetAvg = fidgetAvg / totalCycles;

		CalculatedGazePacket cGp = new CalculatedGazeDataBldr().withTotalTime(totalTime).withFixationsPerMin(FPM)
				.withTimeBtwnFixations(avgTimeBetweenFixations).withAvgFixationLength(avgFixationLength)
				.withSmoothDist(totalSmoothTrackDistance).withPercentTimeFixated(percentTimeFixated)
				.withAvgSaccadeSpeed(avgSaccadeSpeed).withFidgetL(fidgetLp).withFidgetR(fidgetRp)
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
}
