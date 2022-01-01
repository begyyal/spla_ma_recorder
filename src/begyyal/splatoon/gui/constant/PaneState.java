package begyyal.splatoon.gui.constant;

public enum PaneState {
    HOKO(DispRule.HOKO),
    YAGURA(DispRule.YAGURA),
    ASARI(DispRule.ASARI),
    AREA(DispRule.AREA),
    NAWABARI(DispRule.NAWABARI),
    GACHI_ALL(DispRule.GACHI_ALL),
    TOTAL(null);

    public final DispRule dr;

    private PaneState(DispRule dr) {
	this.dr = dr;
    }
    
    public static PaneState parse(DispRule dr) {
	for (PaneState v : values())
	    if (v.dr == dr)
		return v;
	return null;
    } 
}
