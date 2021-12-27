package begyyal.splatoon.object;

import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;

public class DisplayDataBundle {
    public final ObservableList<Data<Number, Number>> data;

    public DisplayDataBundle(
	ObservableList<Data<Number, Number>> data) {
	this.data = data;
    }
}
