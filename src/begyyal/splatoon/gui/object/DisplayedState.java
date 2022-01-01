package begyyal.splatoon.gui.object;

import begyyal.splatoon.gui.constant.PaneState;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class DisplayedState {

    public final ObjectProperty<PaneState> state;
    public final StringProperty rate;

    public DisplayedState() {
	this.state = new SimpleObjectProperty<PaneState>();
	this.rate = new SimpleStringProperty();
    }
}
