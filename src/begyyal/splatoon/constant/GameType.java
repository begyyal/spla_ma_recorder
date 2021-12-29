package begyyal.splatoon.constant;

public enum GameType {
    GACHI(1, "gachi"),
    REGULAR(2, "regular");

    public final int id;
    public final String code;

    private GameType(int id, String code) {
	this.id = id;
	this.code = code;
    }

    public static GameType parse(int id) {
	for (GameType v : values())
	    if (v.id == id)
		return v;
	return null;
    }

    public static GameType parse(String code) {
	for (GameType v : values())
	    if (v.code.equals(code))
		return v;
	return null;
    }
}
