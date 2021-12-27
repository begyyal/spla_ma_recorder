package begyyal.splatoon.gui;

import java.util.Arrays;
import java.util.ResourceBundle;

import begyyal.commons.constant.Strs;
import begyyal.splatoon.object.DisplayDataBundle;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class StageOrganizer {

    private final DisplayDataBundle dataBundle;
    private final Integer[] term;
    private final int windowHeight;
    private final int windowWidth;

    private StageOrganizer(
	Integer[] term,
	int windowHeight,
	int windowWidth,
	DisplayDataBundle dataBundle) {
	this.term = term;
	this.windowHeight = windowHeight;
	this.windowWidth = windowWidth;
	this.dataBundle = dataBundle;
    }

    public static StageOrganizer newi(DisplayDataBundle dataBundle) {

	var res = ResourceBundle.getBundle("common");
	var term = Arrays.stream(res.getString("term").split(Strs.comma))
	    .map(Integer::parseInt)
	    .toArray(Integer[]::new);
	Arrays.sort(term);
	var windowHeight = Integer.parseInt(res.getString("windowHeight"));
	var windowWidth = Integer.parseInt(res.getString("windowWidth"));

	return new StageOrganizer(term, windowHeight, windowWidth, dataBundle);
    }

    public void process(Stage stage) {

	stage.setTitle("Spla2 MA REC");

	final var xAxis = new NumberAxis();
	xAxis.setLabel("How many battle ago (Right end is current)");
	xAxis.autoRangingProperty().setValue(false);
	xAxis.setUpperBound(0);

	final var yAxis = new NumberAxis();
	yAxis.setLabel("Win rate (%)");
	yAxis.autoRangingProperty().setValue(false);
	yAxis.setUpperBound(100);

	final var lineChart = new LineChart<Number, Number>(xAxis, yAxis);
	lineChart.setTitle("Splatoon2 win rates transition (gachi only)");
	lineChart.setPrefSize(this.windowWidth, this.windowHeight - 50);

	var series = new XYChart.Series<Number, Number>();
	series.setName("Win rates");
	series.setData(this.dataBundle.data);
	lineChart.getData().add(series);

	var options = FXCollections.observableArrayList(this.term);
	final var comboBox = new ComboBox<Integer>(options);
	comboBox.valueProperty().addListener(
	    (obs, o, n) -> xAxis.setLowerBound(-n + 1));
	comboBox.setValue(this.term[0]);

	var pane = organizePane(lineChart, comboBox);
	Scene scene = new Scene(pane, this.windowWidth, this.windowHeight);
	stage.setScene(scene);
	stage.show();
    }

    private GridPane organizePane(LineChart<Number, Number> chart, ComboBox<Integer> combo) {

	var grid = new GridPane();
	grid.setVgap(10);
	grid.setHgap(10);
	grid.setPadding(new Insets(10, 10, 10, 10));

	grid.add(new Label("Term : "), 1, 0, 1, 1);
	grid.add(combo, 2, 0, 2, 1);
	grid.add(chart, 0, 1, 10, 6);

	return grid;
    }
}
