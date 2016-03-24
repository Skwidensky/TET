import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import com.theeyetribe.clientsdk.GazeManager;
import com.theeyetribe.clientsdk.IGazeListener;
import com.theeyetribe.clientsdk.data.GazeData;

public class TETData implements IGazeListener {
	// Data structures
	private static volatile ArrayList<double[]> rawFigures = new ArrayList<>();
	private static volatile ArrayList<Boolean> fixationsVsaccades = new ArrayList<>();
	private static volatile ArrayList<String> rawStringOutput = new ArrayList<>();
	private static volatile ArrayList<String> theGoodStuff = new ArrayList<>();
	// GUI
	static JTextArea outputArea = new JTextArea();
	// Ouput and calculation
	static final String RAW_COLUMNS = "timestamp,smoothX,smoothY,pupil_L,pupil_R,fixated";
	static final String TGS_COLUMNS = "total_time,fixations_per_minute,total_bool_fixations,avg_time_between_fixations,avg_length_of_fixation,percent_time_fixated,avg_saccade_speed,blinks,fidget_L_eye,fidget_R_eye,fidget_B_eyes";
	static final double INTERVAL = 33.3333;
	static final double SAMPLE_RATE = 1 / INTERVAL; // 30Hz
	// Number formatter
	static final DecimalFormat d = new DecimalFormat("#.#####");

	public static void main(String[] args) {
		// Keep the text area scrolling with new data
		DefaultCaret caret = (DefaultCaret) outputArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		addRawGazeData(RAW_COLUMNS + System.lineSeparator());
		addTheGoodStuff(TGS_COLUMNS + System.lineSeparator());
		JFrame tetFrame = new JFrame("TET");
		JPanel tetPanel = new JPanel(new FlowLayout());
		JScrollPane outputScrollpane = new JScrollPane(outputArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		outputScrollpane.setPreferredSize(new Dimension(600, 475));
		JButton btnStart, btnStop, btnExport, btnClear;

		btnExport = new JButton("Export");
		btnExport.addActionListener(ae -> {
			export();
		});

		btnStart = new JButton("Start");
		btnStart.addActionListener(ae -> {
			btnStart.setEnabled(false);
			btnExport.setEnabled(false);
			new TETData();
		});

		btnStop = new JButton("Stop");
		btnStop.addActionListener(ae -> {
			GazeManager.getInstance().deactivate();
			btnStart.setEnabled(true);
			btnExport.setEnabled(true);
		});

		btnClear = new JButton("Clear");
		btnClear.addActionListener(ae -> {
			clearList();
		});

		tetPanel.add(outputScrollpane);
		tetPanel.add(btnStart);
		tetPanel.add(btnStop);
		tetPanel.add(btnExport);
		tetPanel.add(btnClear);
		tetFrame.add(tetPanel);
		tetFrame.pack();
		tetFrame.setSize(900, 525);
		tetFrame.setVisible(true);
	}

	// Connect to TET's server for collection of the actual data
	TETData() {
		GazeManager.getInstance().activate(GazeManager.ApiVersion.VERSION_1_0, GazeManager.ClientMode.PUSH);
		GazeManager.getInstance().addGazeListener(this);
	}

	// On each collection cycle
	public void onGazeUpdate(GazeData gazeData) {
		List<Double> verificationList = new ArrayList<>();
		long timestamp = gazeData.timeStamp;
		verificationList.add((double) timestamp);
		double gX = gazeData.smoothedCoordinates.x;
		verificationList.add(gX);
		double gY = gazeData.smoothedCoordinates.y;
		verificationList.add(gY);
		double pL = gazeData.leftEye.pupilSize;
		verificationList.add(pL);
		double pR = gazeData.rightEye.pupilSize;
		verificationList.add(pR);

		boolean fixed = gazeData.isFixated;

		if (gX >= 0 && gY >= 0) {
			// What we'll calculate with
			double[] rawFigures = { (double) timestamp, gX, gY, pL, pR };
			addRawFigures(rawFigures);
			// Don't add fixation boolean if these aren't valid
			fixationsVsaccades.add(fixed);

			// What gets pushed out to file
			String rawGazing = timestamp + "," + gX + "," + gY + "," + pL + "," + pR + "," + fixed
					+ System.lineSeparator();
			addRawGazeData(rawGazing);
		}
	}

	private static void calculateTheGoodStuff() {
		ArrayList<Double> betweenFixations = new ArrayList<>();
		ArrayList<Double> fixationLengths = new ArrayList<>();
		int index = 0;
		double totalFixations = 0, FPM = 0, percentTimeFixated = 0, timesBetweenFixations = 0, fixationLength = 0,
				totalSaccadeTime = 0, totalSaccadeDistance = 0, avgSaccadeSpeed = 0, fidgetinesslP = 0,
				fidgetinessrP = 0, fidgetinessAvg = 0;
		double totalTime = (rawFigures.get(rawFigures.size() - 1)[0] - rawFigures.get(0)[0]);
		double x1 = 0, x2 = 0, y1 = 0, y2 = 0;
		double lP1 = 0, lP2 = 0, rP1 = 0, rP2 = 0;
		int blinks = countBlinks();
		int totalCycles = rawFigures.size();
		for (double[] dArray : rawFigures) {
			if (index < rawFigures.size() && index < fixationsVsaccades.size()) {
				boolean fixated = fixationsVsaccades.get(index);
				// Keeps a tally of the times between fixations
				if (!fixated) {
					timesBetweenFixations += SAMPLE_RATE;
					if (fixationLength > 0) {
						fixationLengths.add(fixationLength);
						fixationLength = 0;
					}
				} else if (timesBetweenFixations > 0) {
					betweenFixations.add(timesBetweenFixations);
					timesBetweenFixations = 0;
				} else {
					fixationLength += SAMPLE_RATE;
				}

				// Points for calculating distance
				x2 = dArray[1];
				y2 = dArray[2];

				// Pupil sizes for calculating fidgetiness
				lP2 = dArray[3];
				rP2 = dArray[4];

				if (fixated) {
					totalFixations++;
				} else {
					// Can't calculate distance from the first index
					if (index != 0 && !fixated) {
						// If I'm not fixated, how far did my eyes move?
						double distance = Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
						totalSaccadeDistance += distance;
					}
				}

				if (index != 0) {
					double deltalP = Math.abs(lP2 - lP1);
					double deltarP = Math.abs(rP2 - rP1);
					fidgetinesslP += deltalP;
					fidgetinessrP += deltarP;
					fidgetinessAvg += (deltalP + deltarP);
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
			for (double d : betweenFixations) {
				avgTimeBetweenFixations += d;
			}
			avgTimeBetweenFixations = (avgTimeBetweenFixations / betweenFixations.size());
		}

		// Find the average length of a fixation
		double avgFixationLength = 0.0;
		if (fixationLengths.size() > 0) {
			for (double d : fixationLengths) {
				avgFixationLength += d;
			}
			avgFixationLength = (avgFixationLength / fixationLengths.size());
		}

		int actualNumFixations = findActualNumberOfFixations();
		FPM = (actualNumFixations / (totalTime / 1000)) * 60;

		percentTimeFixated = (totalFixations * INTERVAL) / totalTime;
		// Time spent saccading
		totalSaccadeTime = (fixationsVsaccades.size() - totalFixations) * INTERVAL;
		// With the time spent and distance, calculate the average speed of a
		// saccade (pixels/millisecond)
		avgSaccadeSpeed = totalSaccadeDistance / totalSaccadeTime;

		fidgetinesslP = fidgetinesslP / totalCycles;
		fidgetinessrP = fidgetinessrP / totalCycles;
		fidgetinessAvg = fidgetinessAvg / totalCycles;

		addTheGoodStuff(totalTime + "," + d.format(FPM) + "," + actualNumFixations + ","
				+ d.format(avgTimeBetweenFixations) + "," + d.format(avgFixationLength) + ","
				+ d.format(percentTimeFixated) + "," + d.format(avgSaccadeSpeed) + "," + blinks + ","
				+ d.format(fidgetinesslP) + "," + d.format(fidgetinessrP) + "," + d.format(fidgetinessAvg));
		System.out.println("Total time(millis): " + totalTime);
		System.out.println("Total fixations(boolean): " + totalFixations);
		System.out.println("Number of actual fixations: " + actualNumFixations);
		System.out.println("Average time between fixations: " + d.format(avgTimeBetweenFixations));
		System.out.println("Average length of a fixation: " + d.format(avgFixationLength));
		System.out.println("Percent time fixated: " + d.format(percentTimeFixated));
		System.out.println("Blinks: " + blinks);
		System.out.println("FPM: " + d.format(FPM));
		System.out.println("Total saccade distance: " + d.format(totalSaccadeDistance));
		System.out.println("Total saccade time: " + totalSaccadeTime);
		System.out.println("Average saccade speed(pixels/millisecond): " + d.format(avgSaccadeSpeed));
		System.out.println("Average fidget delta (left pupil): " + d.format(fidgetinesslP));
		System.out.println("Average fidget delta (right pupil): " + d.format(fidgetinessrP));
		System.out.println("Average fidget delta (both pupils): " + d.format(fidgetinessAvg));
	}

	private static void addRawFigures(double[] pFigures) {
		rawFigures.add(pFigures);
	}

	private static void addRawGazeData(String pGazeData) {
		rawStringOutput.add(pGazeData);
		outputArea.append(pGazeData);
	}

	private static void addTheGoodStuff(String pTheGoodStuff) {
		theGoodStuff.add(pTheGoodStuff);
	}

	private static void clearList() {
		rawFigures.clear();
		fixationsVsaccades.clear();
		rawStringOutput.clear();
		addRawGazeData(RAW_COLUMNS + System.lineSeparator());
		theGoodStuff.clear();
		addTheGoodStuff(TGS_COLUMNS + System.lineSeparator());
		outputArea.setText(RAW_COLUMNS);
	}

	// I count 3 "true" fixation booleans in a row a verified fixation
	private static Integer findActualNumberOfFixations() {
		int actualNumberOfFixations = 0, count = 0;
		for (Boolean b : fixationsVsaccades) {
			if (count == 3) {
				actualNumberOfFixations++;
			}

			if (b) {
				count++;
			} else {
				count = 0;
			}
		}
		return actualNumberOfFixations;
	}

	// Count and remove "blink" instances from rawFigures
	// I count series of empty data of length 2-5 as blink instances.
	// Anything less could simply be dropped data, and anything more is likely
	// to be caused by me shifting out of the vision zone slightly
	private static Integer countBlinks() {
		int blinks = 0, series = 0, index = 0;
		ArrayList<Integer> zeroedIndices = new ArrayList<>();
		boolean blinked = false, endOfRun = false, actualBlink = true;
		for (double[] d : rawFigures) {
			double x = d[1];
			double y = d[2];
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
					if (i < rawFigures.size() - 1) {
						boolean fakeBlink = rawFigures.get(i)[1] == 0.0 && rawFigures.get(i)[2] == 0.0;
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

			if (series < 2 || series > 5) {
				blinked = false;
			} else {
				blinked = true;
			}

			index++;
			if (index == rawFigures.size()) {
				index = index - 1;
			}
		}

		// Get rid of zeroed rows from the data
		for (int i = zeroedIndices.size() - 1; i >= 0; i--) {
			int indexToRemove = zeroedIndices.get(i);
			rawFigures.remove(indexToRemove);
			fixationsVsaccades.remove(indexToRemove);
		}

		return blinks;
	}

	private static void export() {
		calculateTheGoodStuff();
		try {
			FileWriter fw = new FileWriter("C:\\Users\\Charles\\Desktop\\NEWEST_RAWSTRING_OUT.txt", true);
			for (String gazeInstance : rawStringOutput) {
				fw.write(gazeInstance);
			}
			fw.flush();
			fw.close();
			fw = new FileWriter("C:\\Users\\Charles\\Desktop\\NEWEST_GOODSTUFF_OUT.txt", true);
			for (String goodStuff : theGoodStuff) {
				fw.write(goodStuff);
			}
			fw.flush();
			fw.close();
			clearList();
		} catch (IOException f) {
		}
	}
}
