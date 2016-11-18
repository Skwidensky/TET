package tet;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class TransparentHeatmapStage {

	private Stage mStage;
	private AnchorPane mCopyOfHmPane;

	public TransparentHeatmapStage(AnchorPane pHmPane) {
		mStage = new Stage();
		mCopyOfHmPane = pHmPane;
		AnchorPane root = new AnchorPane(mCopyOfHmPane);

		Text close = new MaterialDesignIconView(MaterialDesignIcon.CLOSE);
		close.setStyle("-fx-fill: red; -fx-font-size: 22;");
		StackPane closePane = new StackPane(close);
		closePane.setOnMouseClicked(click -> mStage.close());
		AnchorPane.setTopAnchor(closePane, 20.0);
		AnchorPane.setRightAnchor(closePane, 0.0);
		root.getChildren().add(closePane);

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
