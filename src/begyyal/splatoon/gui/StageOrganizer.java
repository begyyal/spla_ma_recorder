package begyyal.splatoon.gui;

import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import begyyal.commons.constant.Strs;
import begyyal.splatoon.constant.FuncConst;
import begyyal.splatoon.gui.constant.DispGameType;
import begyyal.splatoon.gui.constant.DispRule;
import begyyal.splatoon.gui.constant.PaneState;
import begyyal.splatoon.gui.object.ComponentPalette;
import begyyal.splatoon.gui.object.DispPaneData;
import begyyal.splatoon.gui.object.DisplayedState;
import begyyal.splatoon.object.DisplayDataBundle;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
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

    private final DisplayedState current;

    private StageOrganizer(
	Integer[] term,
	int windowHeight,
	int windowWidth,
	DisplayDataBundle dataBundle) {
	this.term = term;
	this.windowHeight = windowHeight;
	this.windowWidth = windowWidth;
	this.dataBundle = dataBundle;
	this.current = new DisplayedState();
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
	var palette = new ComponentPalette(typeOpt, ruleOpt, termOpt);

	var resolver = new SetupResolver(palette, this.windowHeight, this.windowWidth);
	resolver.setupPaneData(dataBundle);
	resolver.setupPpre();
	resolver.setupLineChart();
	resolver.setupCurrentRate(this.current.rate);

	this.addListenersTo(palette);
	this.setDefaultValuesTo(palette);

	var pane = organizePane(palette);
	Scene scene = new Scene(pane, this.windowWidth, this.windowHeight);
	stage.setScene(scene);
	stage.show();
    }

    private void addListenersTo(ComponentPalette palette) {

	palette.typeCombo.valueProperty().addListener(
	    (obs, o, n) -> {
		this.current.state.set(PaneState.parse(n.rule));
		palette.updateChart(n);
		palette.updateRuleComboItems(n);
		palette.updatePpre(n);
	    });

	palette.ruleCombo.valueProperty().addListener(
	    (obs, o, n) -> {
		// 間接的な更新は全てスルー。明示的に値から値へ変えたときのみ
		if (o != null && n != null && o.type == n.type) {
		    this.current.state.set(PaneState.parse(n));
		    palette.updateChart(n);
		    palette.updatePpre(n);
		}
	    });

	palette.termCombo.valueProperty().addListener(
	    (obs, o, n) -> palette.xAxis.setLowerBound(-n + 1));

	palette.dispDataMap.entrySet().stream()
	    .forEach(e -> this.addListenersToDpd(e.getKey(), e.getValue(), palette));

	this.current.state.addListener((obs, o, n) -> {
	    this.updateCurrentRate(palette.getDpd(this.current.state.get()).series.getData());
	});
    }

    private void addListenersToDpd(PaneState s, DispPaneData dpd, ComponentPalette palette) {
	dpd.series.getData().addListener((Change<? extends XYChart.Data<Number, Number>> c) -> {
	    if (changeIsTarget(c, c2 -> c2.wasReplaced(), s))
		this.updateCurrentRate(c.getList());
	});
	dpd.ppre.addListener((Change<? extends Background> c) -> {
	    if (changeIsTarget(c, c2 -> c2.wasReplaced(), s))
		palette.updatePpre(c.getList());
	});
    }

    private void updateCurrentRate(ObservableList<? extends XYChart.Data<Number, Number>> rates) {
	if (!rates.isEmpty()) {
	    int r = (int) rates.get(rates.size() - 1).getYValue();
	    this.current.rate.set(Integer.toString(r));
	} else
	    this.current.rate.set(Strs.empty);
    }

    private <T> boolean changeIsTarget(Change<T> c, Predicate<Change<T>> cp, PaneState s) {
	while (c.next())
	    if (cp.test(c) && s == this.current.state.get())
		return true;
	return false;
    }

    private void setDefaultValuesTo(ComponentPalette palette) {
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

	grid.add(new Label("Current Rate : "), 27, 0, 1, 1);
	grid.add(palette.rateLabel, 28, 0, 1, 1);

	grid.add(palette.chart, 0, 1, 30, 6);

	return grid;
    }
}
