package tet.weka;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;

public class KNN {
	public static BufferedReader readDataFile(String filename) {
		BufferedReader inputReader = null;

		try {
			inputReader = new BufferedReader(new FileReader("C:\\Users\\Charles\\Desktop\\" + filename));
		} catch (FileNotFoundException ex) {
			System.err.println("File not found: " + filename);
		}

		return inputReader;
	}

	public static void main(String[] args) throws Exception {
		BufferedReader datafile = readDataFile("eyecaff.arff");
		Instances train = new Instances(datafile);
		train.setClassIndex(train.numAttributes() - 1);

		datafile = readDataFile("eyecaff-test.arff");
		Instances test = new Instances(datafile);
		test.setClassIndex(test.numAttributes() - 1);
		datafile.close();

		// Classifier ibk = new IBk();
		// ibk.buildClassifier(data);

		// double class1 = ibk.classifyInstance(first);
		// double class2 = ibk.classifyInstance(second);

//		MultiClassClassifier mcc = new MultiClassClassifier();
//		mcc.buildClassifier(train);
		
		J48 tree = new J48();
		Remove remove = new Remove();
		// filter out columns not to be used in calculation
		String opts[] = new String[]{ "-R", "1,13,14,15,17,18,19,20"};
		remove.setOptions(opts);
		
		FilteredClassifier fc = new FilteredClassifier();
		fc.setFilter(remove);
		fc.setClassifier(tree);
		fc.buildClassifier(train);
//		tree.buildClassifier(train);
		
		
		Instances labeled = new Instances(test);

		// label instances
		for (int i = 0; i < test.numInstances(); i++) {
			double clsLabel = fc.classifyInstance(test.instance(i));
			labeled.instance(i).setClassValue(clsLabel);
		}

		// save labeled data
		BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\Charles\\Desktop\\labeled.arff"));
		writer.write(labeled.toString());
		writer.close();
	}
}