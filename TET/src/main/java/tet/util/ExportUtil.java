package tet.util;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class ExportUtil {
	
	public static void export(String pFilename, ArrayList<String> pOut) {
		try {
			Path path = FileSystems.getDefault().getPath(System.getProperty("user.home") + "\\Desktop", pFilename + ".txt");
			Files.deleteIfExists(path);
			FileWriter fw = new FileWriter(System.getProperty("user.home") + "\\Desktop\\" + pFilename + ".txt", true);
			for (String outputLine : pOut) {
				fw.write(outputLine + "\n");
			}
			fw.flush();
			fw.close();			
		} catch (IOException f) {
		}
	}

}
