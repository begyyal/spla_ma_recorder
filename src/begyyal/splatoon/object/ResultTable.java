package begyyal.splatoon.object;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.Maps;

import begyyal.commons.constant.Strs;
import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.commons.util.object.SuperMap.SuperMapGen;
import begyyal.commons.util.object.TripleList;
import begyyal.commons.util.object.TripleList.TripleListGen;
import begyyal.splatoon.constant.GameType;

public class ResultTable {

    private static final int maInterval = 50;
    // 古->新 | result, 種別レート, 種別無差別レート
    private final TripleList<BattleResult, Integer, Integer> records;

    private ResultTable(TripleList<BattleResult, Integer, Integer> records) {
	this.records = records;
    }

    public static ResultTable of(List<String> plainLines) {
	var records = plainLines.stream()
	    .map(pl -> {
		var st = new StringTokenizer(pl, Strs.comma);
		var battleNum = Integer.parseInt(st.nextToken());
		var isWin = "1".equals(st.nextToken());
		var type = GameType.parse(Integer.parseInt(st.nextToken()));
		return Triple.of(
		    new BattleResult(battleNum, isWin, type),
		    Integer.parseInt(st.nextToken()),
		    Integer.parseInt(st.nextToken()));
	    })
	    .collect(TripleListGen.collect());
	return new ResultTable(records);
    }

    // 保守性を考慮して可読な文字列で
    public List<String> serialize() {
	return this.records.stream()
	    .map(r -> {
		var vArray = new Object[] {
			r.getLeft().id,
			r.getLeft().isWin ? 1 : 0,
			r.getLeft().type.id,
			r.getMiddle(),
			r.getRight() };
		return StringUtils.join(vArray, Strs.comma);
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

	this.records.addAll(results.stream()
	    .map(r -> Triple.of(r, -1, -1))
	    .collect(TripleListGen.collect()));
	var ini = results.get(0).id;
	var startIndexOfNewRange = this.records.indexOf(r -> r.getLeft().id == ini);

	var ratesByType = Arrays.stream(GameType.values())
	    .filter(t -> results.anyMatch(r -> r.type == t))
	    .map(t -> this.calcRatesByType(t, ini))
	    .flatMap(m -> m == null ? Stream.empty() : m.entrySet().stream())
	    .collect(SuperMapGen.collect(e -> e.getKey(), e -> e.getValue()));

	for (int i = startIndexOfNewRange; i < this.records.size(); i++) {
	    var id = this.records.get(i).getLeft().id;
	    if (ratesByType.containsKey(id))
		this.records.setV2(i, ratesByType.get(id));
	    if (i < (maInterval - 1))
		continue;
	    int winRate = (int) this.records
		.subList(i + 1 - maInterval, i + 1)
		.stream()
		.filter(r -> r.getLeft().isWin)
		.count() * 2;
	    this.records.setV3(i, winRate);
	}
    }

    private Map<Integer, Integer> calcRatesByType(GameType type, int ini) {

	var filtered = this.records.stream()
	    .map(r -> r.getLeft())
	    .filter(r -> r.type == type)
	    .collect(SuperListGen.collect());
	if (filtered.size() < maInterval)
	    return null;

	var rateMap = Maps.<Integer, Integer>newHashMap();
	int startIndexOfNewRange = filtered.indexOf(r -> r.id >= ini);
	startIndexOfNewRange = startIndexOfNewRange < (maInterval - 1)
		? (maInterval - 1)
		: startIndexOfNewRange;
	for (int i = startIndexOfNewRange; i < filtered.size(); i++) {
	    int winRate = (int) filtered
		.subList(i + 1 - maInterval, i + 1)
		.stream()
		.filter(r -> r.isWin)
		.count() * 2;
	    rateMap.put(filtered.get(i).id, winRate);
	}

	return rateMap;
    }

    public SuperList<Integer> getWinRates(GameType type) {
	return this.records.stream()
	    .filter(t -> t.getLeft().type == type)
	    .map(t -> t.getMiddle())
	    .filter(r -> r >= 0)
	    .collect(SuperListGen.collect());
    }

    public SuperList<Integer> getTotalWinRates() {
	return this.records.stream()
	    .map(t -> t.getRight())
	    .filter(r -> r >= 0)
	    .collect(SuperListGen.collect());
    }
}
