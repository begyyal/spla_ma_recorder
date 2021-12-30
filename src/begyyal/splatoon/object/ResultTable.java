package begyyal.splatoon.object;

import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import begyyal.commons.constant.Strs;
import begyyal.commons.util.object.PairList;
import begyyal.commons.util.object.PairList.PairListGen;
import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.splatoon.constant.GameType;
import begyyal.splatoon.constant.Rule;
import begyyal.splatoon.processor.RateCalculator;

public class ResultTable {

    private final PairList<BattleResult, RateRecord> records;

    private ResultTable(PairList<BattleResult, RateRecord> records) {
	this.records = records;
    }

    public static ResultTable of(List<String> plainLines) {
	var records = plainLines.stream()
	    .map(ResultTable::deserializeLine)
	    .collect(PairListGen.collect());
	return new ResultTable(records);
    }

    public static Pair<BattleResult, RateRecord> deserializeLine(String pl) {
	var st = new StringTokenizer(pl, Strs.comma);
	var battleNum = Integer.parseInt(st.nextToken());
	var isWin = "1".equals(st.nextToken());
	var type = GameType.parse(Integer.parseInt(st.nextToken()));
	var rule = Rule.parse(Integer.parseInt(st.nextToken()));
	return Pair.of(
	    new BattleResult(battleNum, isWin, type, rule),
	    new RateRecord(
		Integer.parseInt(st.nextToken()),
		Integer.parseInt(st.nextToken()),
		Integer.parseInt(st.nextToken())));
    }

    public List<String> serialize() {
	return this.records.stream()
	    .map(this::serializeRecord)
	    .collect(Collectors.toList());
    }

    public String serializeRecord(Pair<BattleResult, RateRecord> p) {
	var vArray = new Object[] {
		p.getLeft().id,
		p.getLeft().isWin ? 1 : 0,
		p.getLeft().type.id,
		p.getLeft().rule.id,
		p.getRight().ruleRate,
		p.getRight().typeRate,
		p.getRight().totalRate };
	return StringUtils.join(vArray, Strs.comma);
    }

    // 更新があればtrue
    public boolean integrate(SuperList<BattleResult> results) {
	if (results.isEmpty())
	    return false;
	int latestId = this.records.getTip().getLeft().id;
	var startIndexOfNewRange = results.indexOf(r -> r.id == latestId) + 1;
	if (startIndexOfNewRange == 0)
	    this.merge(results);
	else if (startIndexOfNewRange < results.size())
	    this.merge(results.createPartialList(startIndexOfNewRange, results.size()));
	else
	    return false;
	return true;
    }

    private void merge(SuperList<BattleResult> results) {
	this.records.addAll(results.stream()
	    .map(r -> Pair.of(r, new RateRecord()))
	    .collect(PairListGen.collect()));
	new RateCalculator(this.records).exe(results.get(0).id);
    }

    public SuperList<Integer> getWinRates(GameType type) {
	return this.records.stream()
	    .filter(t -> t.getLeft().type == type)
	    .map(t -> t.getRight().typeRate)
	    .filter(r -> r >= 0)
	    .collect(SuperListGen.collect());
    }

    public SuperList<Integer> getWinRates(Rule rule) {
	return this.records.stream()
	    .filter(t -> t.getLeft().rule == rule)
	    .map(t -> t.getRight().ruleRate)
	    .filter(r -> r >= 0)
	    .collect(SuperListGen.collect());
    }

    public SuperList<Integer> getTotalWinRates() {
	return this.records.stream()
	    .map(t -> t.getRight().totalRate)
	    .filter(r -> r >= 0)
	    .collect(SuperListGen.collect());
    }
}
