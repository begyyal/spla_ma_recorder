package begyyal.splatoon.object;

import begyyal.splatoon.constant.GameType;

public class BattleResult {
    public final int id;
    public final boolean isWin;
    public final GameType type;

    public BattleResult(int battleNum, boolean isWin, GameType type) {
	this.id = battleNum;
	this.isWin = isWin;
	this.type = type;
    }

    @Override
    public boolean equals(Object o) {
	if (!(o instanceof BattleResult))
	    return false;
	var casted = (BattleResult) o;
	return this.id == casted.id;
    }
}
