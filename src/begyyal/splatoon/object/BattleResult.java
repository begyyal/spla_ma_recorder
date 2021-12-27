package begyyal.splatoon.object;

public class BattleResult {
    public final int id;
    public final boolean isWin;

    public BattleResult(int battleNum, boolean isWin) {
	this.id = battleNum;
	this.isWin = isWin;
    }

    @Override
    public boolean equals(Object o) {
	if (!(o instanceof BattleResult))
	    return false;
	var casted = (BattleResult) o;
	return this.id == casted.id;
    }
}
