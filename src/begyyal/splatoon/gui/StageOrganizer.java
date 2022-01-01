package begyyal.splatoon.gui;

import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

import begyyal.commons.constant.Strs;
import begyyal.commons.util.object.SuperMap;
import begyyal.commons.util.object.SuperMap.SuperMapGen;
import begyyal.splatoon.constant.FuncConst;
import begyyal.splatoon.gui.constant.DispGameType;
import begyyal.splatoon.gui.constant.DispRule;
import begyyal.splatoon.gui.constant.GuiParts;
import begyyal.splatoon.gui.constant.PaneState;
import begyyal.splatoon.object.DisplayDataBundle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class StageOrganizer {

    private static final DispGameType initType = DispGameType.GACHI;
    private static final DispRule initRule = DispRule.GACHI_ALL;

    private final DisplayDataBundle dataBundle;
    private final Integer[] term;
    private final int windowHeight;
    private final int windowWidth;

    private final ObjectProperty<PaneState> current;

    private StageOrganizer(
	Integer[] term,
	int windowHeight,
	int windowWidth,
	DisplayDataBundle dataBundle) {
	this.term = term;
	this.windowHeight = windowHeight;
	this.windowWidth = windowWidth;
	this.dataBundle = dataBundle;
	this.current = new SimpleObjectProperty<PaneState>();
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
	var palette = new ComponentPalette(
	    typeOpt,
	    ruleOpt,
	    termOpt);

	this.setupPpre(palette);
	this.setupLineChart(palette);
	this.fillPaneData(palette, dataBundle);
	this.addListenersTo(palette);
	this.setValuesTo(palette);

	var pane = organizePane(palette);
	Scene scene = new Scene(pane, this.windowWidth, this.windowHeight);
	stage.setScene(scene);
	stage.show();
    }

    private void fillPaneData(ComponentPalette palette, DisplayDataBundle dataBundle) {

	palette.dispDataMap.entrySet().forEach(e -> {
	    var dr = e.getKey().dr;
	    if (dr == null) {
		e.getValue().series.setName("Win rates / " + DispGameType.TOTAL);
		e.getValue().series.setData(this.dataBundle.totalData.data);
		e.getValue().ppre = this.dataBundle.totalData.ppre;
	    } else {
		e.getValue().series.setName("Win rates / " + dr.type + " / " + dr.label);
		var pd = this.dataBundle.extractData(dr);
		e.getValue().series.setData(pd.data);
		e.getValue().ppre = pd.ppre;
	    }
	});
    }

    private void setupPpre(ComponentPalette palette) {
	IntStream.range(0, FuncConst.ppreCount).forEach(i -> {
	    var l = new Label();
	    l.setText(Integer.toString(i + 1));
	    l.setBorder(GuiParts.plainBorder);
	    l.setLayoutX(i * 20);
	    l.setLayoutY(0);
	    l.setPrefWidth(15);
	    l.setAlignment(Pos.CENTER);
	    palette.ppreGroup.getChildren().add(l);
	});
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
		this.current.set(PaneState.parse(n.rule));
		palette.updateChart(n);
		palette.updateRuleComboItems(n);
		palette.updatePpre(n);
	    });

	palette.ruleCombo.valueProperty().addListener(
	    (obs, o, n) -> {
		// 間接的な更新は全てスルー。明示的に値から値へ変えたときのみ
		if (o != null && n != null && o.type == n.type) {
		    this.current.set(PaneState.parse(n));
		    palette.updateChart(n);
		    palette.updatePpre(n);
		}
	    });

	palette.termCombo.valueProperty().addListener(
	    (obs, o, n) -> palette.xAxis.setLowerBound(-n + 1));

	palette.dispDataMap.entrySet()
	    .stream()
	    .forEach(e -> {
		e.getValue().ppre.addListener((Change<? extends Background> c) -> {
		    while (c.next()) {
			if (c.wasReplaced() && e.getKey() == this.current.get())
			    palette.updatePpre(c.getList());
		    }
		});
	    });
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

	grid.add(new Label("Pararrel Prediction : "), 13, 0, 1, 1);
	grid.add(palette.ppreGroup, 14, 0, FuncConst.ppreCount, 1);

	grid.add(palette.chart, 0, 1, 30, 6);

	return grid;
    }

    private class ComponentPalette {

	private final NumberAxis xAxis;
	private final NumberAxis yAxis;
	private final LineChart<Number, Number> chart;
	private final ComboBox<DispGameType> typeCombo;
	private final ComboBox<DispRule> ruleCombo;
	private final ComboBox<Integer> termCombo;
	private final SuperMap<PaneState, DispPaneData> dispDataMap;
	private final Group ppreGroup;

	private ComponentPalette(
	    ObservableList<DispGameType> obstype,
	    ObservableList<DispRule> obsrule,
	    ObservableList<Integer> obsterm) {

	    this.xAxis = new NumberAxis();
	    this.yAxis = new NumberAxis();
	    this.chart = new LineChart<Number, Number>(xAxis, yAxis);
	    this.typeCombo = new ComboBox<DispGameType>(obstype);
	    this.ruleCombo = new ComboBox<DispRule>(obsrule);
	    this.termCombo = new ComboBox<Integer>(obsterm);
	    this.dispDataMap = Arrays.stream(PaneState.values())
		.collect(SuperMapGen.collect(dr -> dr, dr -> new DispPaneData()));
	    this.ppreGroup = new Group();
	}

	private void updateChart(DispGameType t) {
	    this.chart.getData().clear();
	    this.chart.getData().add(this.getDpd(t.rule).series);
	}

	private void updateChart(DispRule r) {
	    this.chart.getData().clear();
	    this.chart.getData().add(this.getDpd(r).series);
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

	private void updatePpre(DispGameType t) {
	    this.updatePpre(this.getDpd(t.rule).ppre);
	}

	private void updatePpre(DispRule r) {
	    this.updatePpre(this.getDpd(r).ppre);
	}

	private void updatePpre(ObservableList<? extends Background> ppre) {
	    int i = 0;
	    for (var n : this.ppreGroup.getChildren()) {
		if (!(n instanceof Label))
		    continue;
		var label = (Label) n;
		label.setBackground(ppre.get(i++));
	    }
	}

	private DispPaneData getDpd(DispRule dr) {
	    return this.dispDataMap.get(PaneState.parse(dr));
	}
    }

    private class DispPaneData {
	private final Series<Number, Number> series;
	private ObservableList<Background> ppre;

	private DispPaneData() {
	    this.series = new XYChart.Series<Number, Number>();
	}
    }
}
