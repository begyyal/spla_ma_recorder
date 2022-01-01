package begyyal.splatoon.object;

import begyyal.commons.util.object.SuperMap;
import begyyal.commons.util.object.SuperMap.SuperMapGen;
import begyyal.splatoon.constant.GameType;
import begyyal.splatoon.constant.Rule;
import begyyal.splatoon.gui.constant.DispRule;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.layout.Background;

public class DisplayDataBundle {
    public final SuperMap<Rule, PaneData> dataByRule;
    public final SuperMap<GameType, PaneData> dataByType;
    public final PaneData totalData;

    public DisplayDataBundle() {
	this.dataByRule = this.initMap(Rule.values());
	this.dataByType = this.initMap(GameType.values());
	this.totalData = new PaneData();
    }

    private <T> SuperMap<T, PaneData> initMap(T[] tarray) {
	var map = SuperMapGen.<T, PaneData>newi();
	for (var t : tarray)
	    map.put(t, new PaneData());
	return map;
    }

    public PaneData extractData(DispRule dr) {
	return dr.rule == null ? this.dataByType.get(dr.type) : this.dataByRule.get(dr.rule);
    }

    public class PaneData {
	public final ObservableList<Data<Number, Number>> data;
	public final ObservableList<Background> ppre;

	private PaneData() {
	    this.data = FXCollections.observableArrayList();
	    this.ppre = FXCollections.observableArrayList();
	}
    }
}