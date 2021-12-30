package begyyal.splatoon.object;

import begyyal.commons.util.object.SuperMap;
import begyyal.commons.util.object.SuperMap.SuperMapGen;
import begyyal.splatoon.constant.DispRule;
import begyyal.splatoon.constant.GameType;
import begyyal.splatoon.constant.Rule;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;

public class DisplayDataBundle {
    public final SuperMap<Rule, ObservableList<Data<Number, Number>>> dataByRule;
    public final SuperMap<GameType, ObservableList<Data<Number, Number>>> dataByType;
    public final ObservableList<Data<Number, Number>> totalData;

    public DisplayDataBundle() {
	this.dataByRule = this.initMap(Rule.values());
	this.dataByType = this.initMap(GameType.values());
	this.totalData = FXCollections.<Data<Number, Number>>observableArrayList();
    }

    private <T> SuperMap<T, ObservableList<Data<Number, Number>>> initMap(T[] tarray) {
	var map = SuperMapGen.<T, ObservableList<Data<Number, Number>>>newi();
	for (var t : tarray)
	    map.put(t, FXCollections.<Data<Number, Number>>observableArrayList());
	return map;
    }

    public ObservableList<Data<Number, Number>> extractData(DispRule dr) {
	return dr.rule == null ? this.dataByType.get(dr.type) : this.dataByRule.get(dr.rule);
    }
}
