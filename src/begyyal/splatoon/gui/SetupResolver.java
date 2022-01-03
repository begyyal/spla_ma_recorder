package begyyal.splatoon.gui;

import java.util.stream.IntStream;

import begyyal.splatoon.constant.FuncConst;
import begyyal.splatoon.gui.constant.DispGameType;
import begyyal.splatoon.gui.constant.GuiParts;
import begyyal.splatoon.gui.object.ComponentPalette;
import begyyal.splatoon.object.DisplayDataBundle;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;

public class SetupResolver {

    private final ComponentPalette palette;
    private final int windowHeight;
    private final int windowWidth;

    public SetupResolver(ComponentPalette palette, int windowHeight, int windowWidth) {
	this.palette = palette;
	this.windowHeight = windowHeight;
	this.windowWidth = windowWidth;
    }

    public void setupPaneData(DisplayDataBundle dataBundle) {

	this.palette.dispDataMap.entrySet().forEach(e -> {
	    var dr = e.getKey().dr;
	    if (dr == null) {
		e.getValue().series.setName("Win rates / " + DispGameType.TOTAL);
		e.getValue().series.setData(dataBundle.totalData.data);
		e.getValue().ppre = dataBundle.totalData.ppre;
	    } else {
		e.getValue().series.setName("Win rates / " + dr.type + " / " + dr.label);
		var pd = dataBundle.extractData(dr);
		e.getValue().series.setData(pd.data);
		e.getValue().ppre = pd.ppre;
	    }
	});
    }

    public void setupPpre() {
	IntStream.range(0, FuncConst.ppreCount).forEach(i -> {
	    var l = new Label();
	    l.setText(Integer.toString(i + 1));
	    l.setBorder(GuiParts.plainBorder);
	    l.setLayoutX(i * 20);
	    l.setLayoutY(0);
	    l.setPrefWidth(15);
	    l.setAlignment(Pos.CENTER);
	    this.palette.ppreGroup.getChildren().add(l);
	});
    }

    public void setupLineChart() {

	this.palette.xAxis.setLabel("How many battles ago (Right end is current)");
	this.palette.xAxis.autoRangingProperty().setValue(false);
	this.palette.xAxis.setUpperBound(0);

	this.palette.yAxis.setLabel("Win rate (%)");
	this.palette.yAxis.autoRangingProperty().setValue(false);
	this.palette.yAxis.setUpperBound(100);

	this.palette.chart.setTitle("Splatoon2 win rates transition");
	this.palette.chart.setPrefSize(this.windowWidth, this.windowHeight - 50);
	this.palette.chart.setAnimated(false);
	this.palette.chart.setCreateSymbols(false);
    }

    public void setupCurrentRate(StringProperty rateProp) {
	this.palette.rateLabel.textProperty().bind(rateProp);
	this.palette.rateLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold");
    }
}
