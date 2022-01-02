package begyyal.splatoon.gui.constant;

import begyyal.splatoon.constant.GameType;
import begyyal.splatoon.constant.Rule;

public enum DispRule {
    HOKO(GameType.GACHI, Rule.HOKO, "HOKO"),
    YAGURA(GameType.GACHI, Rule.YAGURA, "YAGURA"),
    ASARI(GameType.GACHI, Rule.ASARI, "ASARI"),
    AREA(GameType.GACHI, Rule.AREA, "AREA"),
    NAWABARI(GameType.REGULAR, Rule.NAWABARI, "NAWABARI"),
    GACHI_ALL(GameType.GACHI, null, "ALL");

    private static final DispRule[] gachi = new DispRule[] {
	    GACHI_ALL,
	    HOKO,
	    YAGURA,
	    ASARI,
	    AREA };
    private static final DispRule[] regular = new DispRule[] {
	    NAWABARI };

    public final GameType type;
    public final Rule rule;
    public final String label;

    private DispRule(GameType type, Rule rule, String label) {
	this.type = type;
	this.rule = rule;
	this.label = label;
    }

    public static DispRule[] getBy(GameType type) {
	return type == GameType.GACHI ? gachi
		: type == GameType.REGULAR ? regular : null;
    }
}
