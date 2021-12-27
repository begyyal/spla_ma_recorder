package begyyal.splatoon.object;

import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import begyyal.commons.constant.Strs;
import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;

public class ResultTable {

    // TODO オプション化
    private static final int loggingUpperLimit = 149;
    private static final int maInterval = 50;

    // 古->新の昇順。ちなみにイカリングは降順
    public final SuperList<BattleResult> results;
    public final SuperList<Integer> winRates;
    private int latestId;

    private ResultTable(SuperList<BattleResult> results, SuperList<Integer> winRates,
	int latestId) {
	this.results = results;
	this.winRates = winRates;
	this.latestId = latestId;
    }

    public static ResultTable of(List<String> plainLines) {

	var results = SuperListGen.<BattleResult>of(loggingUpperLimit, l -> l.get(0));
	var winRates = SuperListGen.<Integer>of(loggingUpperLimit + 1 - maInterval, l -> l.get(0));

	int limit = plainLines.size() < loggingUpperLimit ? plainLines.size() : loggingUpperLimit;
	int latestId = -1;
	for (int i = 0; i < limit; i++) {
	    var st = new StringTokenizer(plainLines.get(i), Strs.comma);
	    var battleNum = Integer.parseInt(st.nextToken());
	    var isWin = "1".equals(st.nextToken());
	    results.add(new BattleResult(battleNum, isWin));
	    latestId = battleNum;
	    // 記録を手書きで追記するパターンを考慮してindex<maIntervalを考慮
	    int rate = Integer.parseInt(st.nextToken());
	    if (rate >= 0)
		winRates.add(rate);
	}

	return new ResultTable(results, winRates, latestId);
    }

    // 保守性を考慮して可読な文字列で
    public List<String> serialize() {

	int wCount = (int) this.winRates.stream().filter(w -> w != null).count();
	int rCount = (int) this.results.stream().filter(r -> r != null).count();

	return IntStream.range(0, rCount)
	    .mapToObj(i -> {
		var sb = new StringBuilder();
		sb.append(this.results.get(i).id);
		sb.append(Strs.comma);
		sb.append(this.results.get(i).isWin ? 1 : 0);
		sb.append(Strs.comma);
		// 記録を手書きで追記するパターンを考慮してindex<maIntervalを考慮
		if ((rCount - wCount) <= i)
		    sb.append(this.winRates.get(i - (rCount - wCount)));
		else
		    sb.append(-1);
		return sb.toString();
	    }).collect(Collectors.toList());
    }

    // 更新があればtrue
    public boolean integrate(SuperList<BattleResult> results) {
	var startIndexOfNewRange = results.indexOf(r -> r.id == this.latestId) + 1;
	if (startIndexOfNewRange == 0)
	    this.merge(results);
	else if (startIndexOfNewRange < results.size())
	    this.merge(results.createPartialList(startIndexOfNewRange, results.size()));
	else
	    return false;
	this.latestId = results.getTip().id;
	return true;
    }

    private void merge(SuperList<BattleResult> results) {
	this.results.addAll(results);
	var ini = results.get(0);
	int startIndexOfNewRange = this.results.indexOf(r -> r.equals(ini));
	startIndexOfNewRange = startIndexOfNewRange < (maInterval - 1)
		? (maInterval - 1)
		: startIndexOfNewRange;
	for (int i = startIndexOfNewRange; i < loggingUpperLimit; i++) {
	    if (this.results.get(i) == null)
		break;
	    int winRate = (int) this.results
		.subList(i + 1 - maInterval, i + 1)
		.stream()
		.filter(r -> r.isWin)
		.count() * 2;
	    this.winRates.add(winRate);
	}
    }
}
