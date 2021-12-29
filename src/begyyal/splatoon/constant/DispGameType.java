package begyyal.splatoon.constant;

public enum DispGameType {
    GACHI(GameType.GACHI),
    REGULAR(GameType.REGULAR),
    TOTAL(null);

    public final GameType type;

    private DispGameType(GameType type) {
	this.type = type;
    }
}
