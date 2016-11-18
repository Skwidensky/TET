package tet.util;

import eu.hansolo.fx.heatmap.ColorMapping;
import eu.hansolo.fx.heatmap.HeatMap;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class HeatMapProvider {
	private StackPane mHmPane;
	private HeatMap mHeatMap;

	public HeatMapProvider() {}

	public StackPane getHeatMapPane() {
		return mHmPane;
	}
	
	public HeatMap getHeatMap() {
		return mHeatMap;
	}
	
	public void createNewHm(double pWidth, double pHeight) {
		mHmPane = new StackPane();
		mHeatMap = new HeatMap(pWidth, pHeight, ColorMapping.BLUE_CYAN_GREEN_YELLOW_RED);
		mHmPane.getChildren().add(mHeatMap);
		registerListeners();		
	}

	private void registerListeners() {
		mHmPane.widthProperty()
				.addListener((ov, oldWidth, newWidth) -> mHeatMap.setSize(newWidth.doubleValue(), mHmPane.getHeight()));
		mHmPane.heightProperty()
				.addListener((ov, oldHeight, newHeight) -> mHeatMap.setSize(mHmPane.getWidth(), newHeight.doubleValue()));
	}

}
