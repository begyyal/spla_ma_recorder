package begyyal.splatoon.gui.object;

import java.util.Arrays;
import java.util.Map.Entry;

import begyyal.commons.util.object.SuperMap;
import begyyal.commons.util.object.SuperMap.SuperMapGen;
import begyyal.splatoon.gui.constant.DispGameType;
import begyyal.splatoon.gui.constant.DispRule;
import begyyal.splatoon.gui.constant.PaneState;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;

public class ComponentPalette {
    public final NumberAxis xAxis;
    public final NumberAxis yAxis;
    public final LineChart<Number, Number> chart;
    public final ComboBox<DispGameType> typeCombo;
    public final ComboBox<DispRule> ruleCombo;
    public final ComboBox<Integer> termCombo;
    public final SuperMap<PaneState, DispPaneData> dispDataMap;
    public final Group ppreGroup;
    public final Label rateLabel;

    public ComponentPalette(
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
	this.rateLabel = new Label();
    }

    public void updateChart(DispGameType t) {
	this.chart.getData().clear();
	this.chart.getData().add(this.getDpd(t.rule).series);
    }

    public void updateChart(DispRule r) {
	this.chart.getData().clear();
	this.chart.getData().add(this.getDpd(r).series);
    }

    public void updateRuleComboItems(DispGameType dt) {
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

    public void updatePpre(DispGameType t) {
	this.updatePpre(this.getDpd(t.rule).ppre);
    }

    public void updatePpre(DispRule r) {
	this.updatePpre(this.getDpd(r).ppre);
    }

    public void updatePpre(ObservableList<? extends Background> ppre) {
	int i = 0;
	for (var n : this.ppreGroup.getChildren()) {
	    if (!(n instanceof Label))
		continue;
	    var label = (Label) n;
	    label.setBackground(ppre.get(i++));
	}
    }

    public DispPaneData getDpd(DispRule dr) {
	return this.dispDataMap.get(PaneState.parse(dr));
    }

    public DispPaneData getDpd(PaneState state) {
	return this.dispDataMap.entrySet().stream()
	    .filter(e -> e.getKey() == state)
	    .map(Entry::getValue)
	    .findFirst().get();
    }
}
