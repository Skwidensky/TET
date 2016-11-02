package tet.util;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class ExportUtil {
	
	public static void export(ArrayList<String> pRawOut, ArrayList<String> pCalcOut) {
		try {
			Path path = FileSystems.getDefault().getPath(System.getProperty("user.home") + "\\Desktop", "NEWEST_RAWSTRING_OUT.txt");
			Files.deleteIfExists(path);
			FileWriter fw = new FileWriter(System.getProperty("user.home") + "\\Desktop\\NEWEST_RAWSTRING_OUT.txt", true);
			for (String gazeInstance : pRawOut) {
				fw.write(gazeInstance);
			}
			fw.flush();
			fw.close();
			path = FileSystems.getDefault().getPath(System.getProperty("user.home") + "\\Desktop", "NEWEST_GOODSTUFF_OUT.txt");
			Files.deleteIfExists(path);
			fw = new FileWriter(System.getProperty("user.home") + "\\Desktop\\NEWEST_GOODSTUFF_OUT.txt", true);
			for (String goodStuff : pCalcOut) {
				fw.write(goodStuff);
			}
			fw.flush();
			fw.close();
			
		} catch (IOException f) {
		}
	}

}
