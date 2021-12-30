package begyyal.splatoon.gui;

import java.util.Arrays;
import java.util.ResourceBundle;

import org.apache.commons.lang3.tuple.Pair;

import begyyal.commons.constant.Strs;
import begyyal.commons.util.object.SuperMap;
import begyyal.commons.util.object.SuperMap.SuperMapGen;
import begyyal.splatoon.constant.DispGameType;
import begyyal.splatoon.constant.DispRule;
import begyyal.splatoon.object.DisplayDataBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class StageOrganizer {

    private static final DispGameType initType = DispGameType.GACHI;
    private static final DispRule initRule = DispRule.GACHI_ALL;

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

	var typeOpt = FXCollections.observableArrayList(DispGameType.values());
	var ruleOpt = initType.type == null
		? FXCollections.<DispRule>observableArrayList()
		: FXCollections.observableArrayList(DispRule.getBy(initType.type));
	var termOpt = FXCollections.observableArrayList(term);

	var seriesMap = Arrays.stream(DispRule.values()).map(dr -> {
	    var series = new XYChart.Series<Number, Number>();
	    series.setName("Win rates / " + dr.type + " / " + dr.label);
	    series.setData(this.dataBundle.extractData(dr));
	    return Pair.of(dr, series);
	}).collect(SuperMapGen.collect(p -> p.getLeft(), p -> p.getValue()));
	var totalSeries = new XYChart.Series<Number, Number>();
	totalSeries.setName("Win rates / " + DispGameType.TOTAL);
	totalSeries.setData(this.dataBundle.totalData);

	var palette = new ComponentPalette(
	    typeOpt,
	    ruleOpt,
	    termOpt,
	    seriesMap,
	    totalSeries);

	this.setupLineChart(palette);

	this.addListenersTo(palette);
	this.setValuesTo(palette);

	var pane = organizePane(palette);
	Scene scene = new Scene(pane, this.windowWidth, this.windowHeight);
	stage.setScene(scene);
	stage.show();
    }

    private void setupLineChart(ComponentPalette palette) {

	palette.xAxis.setLabel("How many battles ago (Right end is current)");
	palette.xAxis.autoRangingProperty().setValue(false);
	palette.xAxis.setUpperBound(0);

	palette.yAxis.setLabel("Win rate (%)");
	palette.yAxis.autoRangingProperty().setValue(false);
	palette.yAxis.setUpperBound(100);

	palette.chart.setTitle("Splatoon2 win rates transition");
	palette.chart.setPrefSize(this.windowWidth, this.windowHeight - 50);
    }

    private void addListenersTo(ComponentPalette palette) {

	palette.typeCombo.valueProperty().addListener(
	    (obs, o, n) -> {
		palette.updateChart(n);
		palette.updateRuleComboItems(n);
	    });

	palette.ruleCombo.valueProperty().addListener(
	    (obs, o, n) -> {
		if (o != null && o.type == n.type)
		    palette.updateChart(n);
	    });

	palette.termCombo.valueProperty().addListener(
	    (obs, o, n) -> palette.xAxis.setLowerBound(-n + 1));
    }

    private void setValuesTo(ComponentPalette palette) {
	palette.typeCombo.setValue(initType);
	palette.ruleCombo.setValue(initRule);
	palette.termCombo.setValue(this.term[0]);
    }

    private GridPane organizePane(ComponentPalette palette) {

	var grid = new GridPane();
	grid.setVgap(10);
	grid.setHgap(10);
	grid.setPadding(new Insets(10, 10, 10, 10));

	grid.add(new Label("Type : "), 1, 0, 1, 1);
	grid.add(palette.typeCombo, 2, 0, 2, 1);

	grid.add(new Label("Rule : "), 5, 0, 1, 1);
	grid.add(palette.ruleCombo, 6, 0, 2, 1);

	grid.add(new Label("Term : "), 9, 0, 1, 1);
	grid.add(palette.termCombo, 10, 0, 2, 1);

	grid.add(palette.chart, 0, 1, 20, 6);

	return grid;
    }

    private class ComponentPalette {

	private final NumberAxis xAxis;
	private final NumberAxis yAxis;
	private final LineChart<Number, Number> chart;
	private final ComboBox<DispGameType> typeCombo;
	private final ComboBox<DispRule> ruleCombo;
	private final ComboBox<Integer> termCombo;
	private final SuperMap<DispRule, Series<Number, Number>> seriesMap;
	private final Series<Number, Number> totalSeries;

	private ComponentPalette(
	    ObservableList<DispGameType> obstype,
	    ObservableList<DispRule> obsrule,
	    ObservableList<Integer> obsterm,
	    SuperMap<DispRule, Series<Number, Number>> seriesMap,
	    Series<Number, Number> totalSeries) {

	    this.xAxis = new NumberAxis();
	    this.yAxis = new NumberAxis();
	    this.chart = new LineChart<Number, Number>(xAxis, yAxis);
	    this.typeCombo = new ComboBox<DispGameType>(obstype);
	    this.ruleCombo = new ComboBox<DispRule>(obsrule);
	    this.termCombo = new ComboBox<Integer>(obsterm);
	    this.seriesMap = seriesMap;
	    this.totalSeries = totalSeries;
	}

	private void updateChart(DispGameType t) {
	    this.chart.getData().clear();
	    this.chart.getData()
		.add(t == DispGameType.TOTAL
			? this.totalSeries
			: t == DispGameType.REGULAR
				? this.seriesMap.get(DispRule.NAWABARI)
				: this.seriesMap.get(DispRule.GACHI_ALL));
	}

	private void updateChart(DispRule r) {
	    this.chart.getData().clear();
	    this.chart.getData().add(this.seriesMap.get(r));
	}

	private void updateRuleComboItems(DispGameType dt) {
	    if (dt != DispGameType.TOTAL) {
		this.ruleCombo.setDisable(false);
		var items = DispRule.getBy(dt.type);
		this.ruleCombo.getItems().setAll(items);
		var v = this.ruleCombo.getValue();
		if (v != null && Arrays.binarySearch(items, v) < 0)
		    this.ruleCombo.setValue(items[0]);
	    } else
		this.ruleCombo.setDisable(true);
	}
    }
}
