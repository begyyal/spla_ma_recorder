package begyyal.splatoon.gui.constant;

import begyyal.splatoon.constant.GameType;

public enum DispGameType {
    GACHI(DispRule.GACHI_ALL, GameType.GACHI),
    REGULAR(DispRule.NAWABARI, GameType.REGULAR),
    TOTAL(null, null);

    public final DispRule rule;
    public final GameType type;

    private DispGameType(DispRule rule, GameType type) {
	this.rule = rule;
	this.type = type;
    }
}
