package begyyal.splatoon.object;

import begyyal.commons.util.object.SuperMap;
import begyyal.commons.util.object.SuperMap.SuperMapGen;
import begyyal.splatoon.constant.DispGameType;
import begyyal.splatoon.constant.GameType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;

public class DisplayDataBundle {
    public final SuperMap<GameType, ObservableList<Data<Number, Number>>> dataByType;
    public final ObservableList<Data<Number, Number>> data;

    public DisplayDataBundle() {
	this.dataByType = SuperMapGen.newi();
	this.data = FXCollections.<Data<Number, Number>>observableArrayList();
	for (var t : GameType.values())
	    this.dataByType.put(t, FXCollections.<Data<Number, Number>>observableArrayList());
    }

    public ObservableList<Data<Number, Number>> getDataByType(GameType t) {
	return this.dataByType.get(t);
    }

    public ObservableList<Data<Number, Number>> extractData(DispGameType dt) {
	return dt.type == null ? this.data : this.getDataByType(dt.type);
    }
}
