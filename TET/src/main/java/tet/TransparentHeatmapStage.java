package tet;

import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class TransparentHeatmapStage {
	
	private Stage mStage;
	
	public TransparentHeatmapStage(AnchorPane pHmPane) {
		mStage = new Stage();	
		StackPane root = new StackPane(pHmPane);
		root.setStyle("-fx-background-color: transparent;");
		double width = Screen.getPrimary().getBounds().getWidth();
		double height = Screen.getPrimary().getBounds().getHeight();
		Scene scene = new Scene(root, width, height);
		scene.setFill(null);
		mStage.setScene(scene);
		mStage.initStyle(StageStyle.TRANSPARENT);		
	}
	
	public Stage getTransparentStage() {
		return mStage;
	}

}
