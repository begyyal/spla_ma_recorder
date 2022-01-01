package begyyal.splatoon.constant;

// 現状未対応
public enum Weapon {
    SPLA_SHOOTER(40, "SPLA_SHOOTER"),
    A_GALLON_DECO(51, "52GALLON_DECO"),
    N_ZAP_A(60, "N_ZAP_85"),
    HOT_BLASTER(210, "HOT_BLASTER"),
    DYNAMO(1020, "DYNAMO"),
    VARIABLE_FOIL(1031, "VARIABLE_FOIL"),
    HYDRANT(4020, "HYDRANT"),
    NAUTILUS_A(4040, "NAUTILUS_47");

    public final int id;
    public final String description;

    private Weapon(int id, String description) {
	this.id = id;
	this.description = description;
    }

    public static Weapon parse(int id) {
	for (Weapon v : values())
	    if (v.id == id)
		return v;
	return null;
    }
}
