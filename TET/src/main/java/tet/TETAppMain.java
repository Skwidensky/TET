package tet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class TETAppMain extends Application {
	
	private static final Logger sLog = LoggerFactory.getLogger(TETAppMain.class);
	
	private static ReadOnlyDoubleProperty sStageWidth, sStageHeight;	
	private final StackPane mRoot = new StackPane();
	
	public static void main(String[] pArgs) {
		Application.launch(pArgs);
	}

	@Override
	public void start(Stage pPrimaryStage) throws Exception {
		
		sLog.info("Starting TET-FX application");
		
		double width = 750;
		double height = 550;
		mRoot.setPrefWidth(width);
		mRoot.setPrefHeight(height);
		mRoot.setMinWidth(width);
		mRoot.setMinHeight(height);
		
		mRoot.getChildren().setAll(createView());
		
		Scene scene = new Scene(mRoot, width, height);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		pPrimaryStage.setScene(scene);
		pPrimaryStage.show();
		sStageWidth = pPrimaryStage.widthProperty();	
		sStageHeight = pPrimaryStage.heightProperty();
	}
	
	private static Node createView() {
		return MainViewFactory.instance().getView();
	}
	
	public static ReadOnlyDoubleProperty getStageWidth() {
		return sStageWidth;
	}
	
	public static ReadOnlyDoubleProperty getStageHeight() {
		return sStageHeight;
	}
}