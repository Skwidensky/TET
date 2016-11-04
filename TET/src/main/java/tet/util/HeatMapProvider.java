package tet.util;

import eu.hansolo.fx.heatmap.ColorMapping;
import eu.hansolo.fx.heatmap.HeatMap;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public class HeatMapProvider {
	private StackPane mHmPane;
	private HeatMap mHeatMap;

	public HeatMapProvider() {
		mHmPane = new StackPane();
		mHeatMap = new HeatMap(400, 400, ColorMapping.BLUE_CYAN_GREEN_YELLOW_RED);
		mHeatMap.setOpacity(1.0);
		mHmPane.getChildren().add(mHeatMap);
		registerListeners();
	}

	public StackPane getHeatMapPane() {
		return mHmPane;
	}

	private void registerListeners() {
		mHmPane.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
			double x = event.getX();
			double y = event.getY();
			if (x < mHeatMap.getEventRadius())
				x = mHeatMap.getEventRadius();
			if (x > mHmPane.getWidth() - mHeatMap.getEventRadius())
				x = mHmPane.getWidth() - mHeatMap.getEventRadius();
			if (y < mHeatMap.getEventRadius())
				y = mHeatMap.getEventRadius();
			if (y > mHmPane.getHeight() - mHeatMap.getEventRadius())
				y = mHmPane.getHeight() - mHeatMap.getEventRadius();

			mHeatMap.addEvent(x, y);
		});
		mHmPane.widthProperty()
				.addListener((ov, oldWidth, newWidth) -> mHeatMap.setSize(newWidth.doubleValue(), mHmPane.getHeight()));
		mHmPane.heightProperty()
				.addListener((ov, oldHeight, newHeight) -> mHeatMap.setSize(mHmPane.getWidth(), newHeight.doubleValue()));
	}

}
