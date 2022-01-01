package begyyal.splatoon.object;

import begyyal.splatoon.constant.GameType;
import begyyal.splatoon.constant.Rule;

public class BattleResult {
    public final int id;
    public final boolean isWin;
    public final GameType type;
    public final Rule rule;
    public final int weaponId;

    public BattleResult(
	int battleNum,
	boolean isWin,
	GameType type,
	Rule rule,
	int weaponId) {
	this.id = battleNum;
	this.isWin = isWin;
	this.type = type;
	this.rule = rule;
	this.weaponId = weaponId;
    }

    @Override
    public boolean equals(Object o) {
	if (!(o instanceof BattleResult))
	    return false;
	var casted = (BattleResult) o;
	return this.id == casted.id;
    }
}
