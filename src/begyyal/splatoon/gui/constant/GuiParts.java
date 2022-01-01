package begyyal.splatoon.gui.constant;

import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

public class GuiParts {
    public static final Border plainBorder = new Border(
	new BorderStroke(
	    Color.BLACK,
	    BorderStrokeStyle.SOLID,
	    CornerRadii.EMPTY,
	    BorderWidths.DEFAULT));
    public static final Background bkgWhite = new Background(
	new BackgroundFill(
	    Color.WHITE,
	    CornerRadii.EMPTY,
	    Insets.EMPTY));
    public static final Background bkgRed = new Background(
	new BackgroundFill(
	    Color.PINK,
	    CornerRadii.EMPTY,
	    Insets.EMPTY));
    public static final Background bkgBlue = new Background(
	new BackgroundFill(
	    Color.LIGHTBLUE,
	    CornerRadii.EMPTY,
	    Insets.EMPTY));
}
