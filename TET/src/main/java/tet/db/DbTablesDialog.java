package tet.db;

import java.util.ArrayList;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tet.util.ExportUtil;

public class DbTablesDialog extends StackPane {

	StackPane mParent;
	ListView<String> mDbListView;
	ObservableList<String> mListOfTables;
	HBox mButtonBank;
	IntegerBinding mListSizeBinding;

	public DbTablesDialog(StackPane pParent) {
		setStyle("-fx-background-color: white;");
		mParent = pParent;
		initListView();
		initButtons();
		VBox mainVbox = new VBox(5, new Label("Tables"), mDbListView, mButtonBank);
		getChildren().add(mainVbox);
	}

	private void initListView() {
		String[] command = new String[1];
		command[0] = sSqlCmds.getListOfTables();
		DatabaseMgr.sqlCommand(true, "", command);
		mListOfTables = FXCollections.observableArrayList(DatabaseMgr.getQuery());
		mDbListView = new ListView<>(mListOfTables);
		mListSizeBinding  = Bindings.size(mListOfTables);
	}

	private void initButtons() {
		Button backBtn = new Button("Back");
		backBtn.setOnAction(event -> mParent.getChildren().remove(this));

		Button dropBtn = new Button("Drop");
		dropBtn.setOnAction(event -> drop());
		dropBtn.disableProperty().bind(mListSizeBinding.greaterThan(0).not());

		Button exportBtn = new Button("Export");
		exportBtn.setOnAction(event -> export());
		exportBtn.disableProperty().bind(mListSizeBinding.greaterThan(0).not());

		mButtonBank = new HBox(5, backBtn, dropBtn, exportBtn);
	}

	private void drop() {
		String tbl = mDbListView.getSelectionModel().getSelectedItem();
		mListOfTables.remove(tbl);
		String[] command = new String[1];
		command[0] = sSqlCmds.dropTbl(tbl);
		DatabaseMgr.sqlCommand(false, "", command);
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
}