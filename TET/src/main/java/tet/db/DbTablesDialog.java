package tet.db;

import java.util.ArrayList;
import java.util.List;

import eu.hansolo.fx.heatmap.HeatMap;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import tet.RawGazePacket;
import tet.TransparentHeatmapStage;
import tet.util.ExportUtil;
import tet.util.HeatMapProvider;

public class DbTablesDialog extends StackPane {

	private StackPane mParent;
	private ListView<String> mDbListView;
	private ObservableList<String> mListOfTables;
	private HBox mButtonBank;
	private HeatMapProvider mHmProvider;
	private AnchorPane mBigHeatMapPane;
	private HeatMap mHm;
	private IntegerBinding mListSizeBinding;
	private final BooleanProperty mIsCalculatedTbl = new SimpleBooleanProperty(false);

	public DbTablesDialog(StackPane pParent) {
		setStyle("-fx-background-color: white;");
		mParent = pParent;
		initListView();
		initButtons();
		initHeatMap();
		VBox mainVbox = new VBox(5, new Label("Tables"), mDbListView, mButtonBank);
		getChildren().add(mainVbox);
	}

	private void initListView() {
		String[] command = new String[1];
		command[0] = sSqlCmds.getListOfTables();
		DatabaseMgr.sqlCommand(true, "", command);
		mListOfTables = FXCollections.observableArrayList(DatabaseMgr.getQuery());
		mDbListView = new ListView<>(mListOfTables);
		mDbListView.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
			if (mDbListView.getSelectionModel().getSelectedItem().contains("_calculated")) {
				mIsCalculatedTbl.set(true);
			} else {
				mIsCalculatedTbl.set(false);
			}
		});
		mListSizeBinding = Bindings.size(mListOfTables);
	}

	private void initButtons() {
		Button backBtn = new Button("Back");
		backBtn.setOnAction(event -> mParent.getChildren().remove(this));

		Button dropBtn = new Button("Drop");
		dropBtn.setOnAction(event -> drop());
		dropBtn.disableProperty().bind(mListSizeBinding.greaterThan(0).not()
				.or(mDbListView.getSelectionModel().selectedIndexProperty().isEqualTo(-1)));

		Button exportBtn = new Button("Export");
		exportBtn.setOnAction(event -> export());
		exportBtn.disableProperty().bind(mListSizeBinding.greaterThan(0).not()
				.or(mDbListView.getSelectionModel().selectedIndexProperty().isEqualTo(-1)));

		Button heatmapBtn = new Button("Heat Map");
		heatmapBtn.setOnAction(event -> showTransparentHeatmap());
		heatmapBtn.disableProperty().bind(mListSizeBinding.greaterThan(0).not()
				.or(mDbListView.getSelectionModel().selectedIndexProperty().isEqualTo(-1)).or(mIsCalculatedTbl));

		mButtonBank = new HBox(5, backBtn, dropBtn, exportBtn, heatmapBtn);
	}
	
	private void initHeatMap() {
		mHmProvider = new HeatMapProvider();
		mHmProvider.createNewHm(Screen.getPrimary().getBounds().getWidth(),
				Screen.getPrimary().getBounds().getHeight());
		mHm = mHmProvider.getHeatMap();
		mBigHeatMapPane = new AnchorPane(mHm);
	}

	private void drop() {
		String tblBaseName = getBaseTblName(mDbListView.getSelectionModel().getSelectedItem());

		for (String tbl : mListOfTables) {
			if (getBaseTblName(tbl).equals(tblBaseName)) {
				String[] command = new String[1];
				command[0] = sSqlCmds.dropTbl(tbl);
				DatabaseMgr.sqlCommand(false, "", command);
			}
		}
		mListOfTables.removeIf(p -> getBaseTblName(p).equals(tblBaseName));
	}

	private void export() {
		ArrayList<String> exportData = new ArrayList<>();
		String[] command = new String[1];
		command[0] = "select * from " + mDbListView.getSelectionModel().getSelectedItem() + ";";
		DatabaseMgr.sqlCommand(true, "getRows", command);
		DatabaseMgr.getQuery().stream().forEach(rawOutput -> {
			exportData.add(rawOutput);
		});

		if (exportData.size() > 1) {
			ExportUtil.export(mDbListView.getSelectionModel().getSelectedItem(), exportData);
		}
	}

	private void showTransparentHeatmap() {
		mHm.clearHeatMap();
		TransparentHeatmapStage hmStage = new TransparentHeatmapStage(mBigHeatMapPane);
		hmStage.getTransparentStage().show();
		List<Point2D> listOfPoints = new ArrayList<>();
		ArrayList<RawGazePacket> rgps = getThisTestsRgps();
		for (RawGazePacket rgp : rgps) {
			listOfPoints.add(new Point2D(rgp.getGazeX(), rgp.getGazeY()));
		}
		mHm.addEvents(listOfPoints);
	}
	
	private ArrayList<RawGazePacket> getThisTestsRgps() {
		ArrayList<RawGazePacket> returnList = new ArrayList<>();
		String[] command = new String[1];
		command[0] = sSqlCmds.getRaw(getBaseTblName(mDbListView.getSelectionModel().getSelectedItem()));
		DatabaseMgr.sqlCommand(true, "raw_sample", command);
		DatabaseMgr.getQuery().stream().forEach(rawOutput -> {
			returnList.add(new RawGazePacket.RawGazeDataBldr().from(rawOutput).build());
		});
		return returnList;
	}
	
	private String getBaseTblName(String pTbl) {
		return pTbl.substring(0, pTbl.indexOf("_", 0));
	}
}