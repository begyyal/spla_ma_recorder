package begyyal.splatoon.gui;

import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class StageOrganizer {

    private final ObservableList<Data<Number, Number>> data;

    private StageOrganizer(ObservableList<Data<Number, Number>> data) {
	this.data = data;
    }

    public static StageOrganizer newi(ObservableList<Data<Number, Number>> data) {
	return new StageOrganizer(data);
    }

    public void process(Stage stage) {
	
	stage.setTitle("Spla2 MA REC");
	
	final var xAxis = new NumberAxis();
	xAxis.setLabel("How many battle ago (Right end is current)");
	xAxis.autoRangingProperty().setValue(false);
	xAxis.setUpperBound(0);
	xAxis.setLowerBound(-99);
	
	final var yAxis = new NumberAxis();
	yAxis.setLabel("Win rate");
	yAxis.autoRangingProperty().setValue(false);
	yAxis.setUpperBound(100);
	
	final var lineChart = new LineChart<Number, Number>(xAxis, yAxis);
	lineChart.setTitle("Splatoon2 win rates transition");
	
	var series = new XYChart.Series<Number, Number>();
	series.setName("Win rates");
	series.setData(this.data);
	lineChart.getData().add(series);

	StackPane root = new StackPane();
	root.getChildren().add(lineChart);
	
	Scene scene = new Scene(root, 1000, 700);
	stage.setScene(scene);
	stage.show();
    }
}
