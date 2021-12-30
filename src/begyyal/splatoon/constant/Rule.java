package begyyal.splatoon.constant;

public enum Rule {
    HOKO(1, "rainmaker"),
    YAGURA(2, "tower_control"),
    ASARI(3, "clam_blitz"),
    AREA(4, "splat_zones"),
    NAWABARI(5, "turf_war");

    public final int id;
    public final String code;

    private Rule(int id, String code) {
	this.id = id;
	this.code = code;
    }

    public static Rule parse(int id) {
	for (Rule v : values())
	    if (v.id == id)
		return v;
	return null;
    }

    public static Rule parse(String code) {
	for (Rule v : values())
	    if (v.code.equals(code))
		return v;
	return null;
    }
}
