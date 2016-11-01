package tet;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class MainViewFactory {
	
	private static final Logger sLog = LoggerFactory.getLogger(TETAppMain.class);
	
	private static class Lazy {
		static final MainViewFactory sInstance = new MainViewFactory();
	}
	
	public static MainViewFactory instance() {
		return Lazy.sInstance;
	}

	public Parent getView() {
		try {
			sLog.info("Loading main.fxml view");
			return FXMLLoader.load(getClass().getResource("main.fxml"));
		} catch(IOException pE) {
			sLog.error("Can't find main.fxml");
		}
		return null;
	}
}
