package begyyal.splatoon.object;

import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import begyyal.commons.constant.Strs;
import begyyal.commons.util.object.PairList;
import begyyal.commons.util.object.PairList.PairListGen;
import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;

public class ResultTable {

    private static final int maInterval = 50;
    // 古->新の昇順。ちなみにイカリングは降順
    private final PairList<BattleResult, Integer> records;

    private ResultTable(PairList<BattleResult, Integer> records) {
	this.records = records;
    }

    public static ResultTable of(List<String> plainLines) {
	var records = plainLines.stream()
	    .map(pl -> {
		var st = new StringTokenizer(pl, Strs.comma);
		var battleNum = Integer.parseInt(st.nextToken());
		var isWin = "1".equals(st.nextToken());
		return Pair.of(
		    new BattleResult(battleNum, isWin),
		    Integer.parseInt(st.nextToken()));
	    })
	    .collect(PairListGen.collect());
	return new ResultTable(records);
    }

    // 保守性を考慮して可読な文字列で
    public List<String> serialize() {
	return this.records.stream()
	    .map(r -> {
		var sb = new StringBuilder();
		sb.append(r.getLeft().id);
		sb.append(Strs.comma);
		sb.append(r.getLeft().isWin ? 1 : 0);
		sb.append(Strs.comma);
		sb.append(r.getRight());
		return sb.toString();
	    }).collect(Collectors.toList());
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
	this.records.addAll(
	    results.stream().map(r -> Pair.of(r, -1)).collect(PairListGen.collect()));
	var ini = results.get(0);
	int startIndexOfNewRange = this.records.indexOf(r -> r.getLeft().equals(ini));
	startIndexOfNewRange = startIndexOfNewRange < (maInterval - 1)
		? (maInterval - 1)
		: startIndexOfNewRange;
	for (int i = startIndexOfNewRange; i < this.records.size(); i++) {
	    int winRate = (int) this.records
		.subList(i + 1 - maInterval, i + 1)
		.stream()
		.filter(r -> r.getLeft().isWin)
		.count() * 2;
	    this.records.setV2(i, winRate);
	}
    }

    public SuperList<Integer> getWinRates() {
	return this.records.stream()
	    .map(p -> p.getRight())
	    .filter(r -> r >= 0)
	    .collect(SuperListGen.collect());
    }
}
