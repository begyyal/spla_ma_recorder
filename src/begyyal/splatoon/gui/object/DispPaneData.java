package begyyal.splatoon.gui.object;

import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.Background;

public class DispPaneData {
    public final Series<Number, Number> series;
    public ObservableList<Background> ppre;

    public DispPaneData() {
	this.series = new XYChart.Series<Number, Number>();
    }
}
