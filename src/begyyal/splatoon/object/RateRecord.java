package begyyal.splatoon.object;

public class RateRecord {
    public int ruleRate;
    public int typeRate;
    public int totalRate;

    public RateRecord() {
	this.ruleRate = -1;
	this.typeRate = -1;
	this.totalRate = -1;
    }

    public RateRecord(
	int ruleRate,
	int typeRate,
	int totalRate) {
	this.ruleRate = ruleRate;
	this.typeRate = typeRate;
	this.totalRate = totalRate;
    }
}
