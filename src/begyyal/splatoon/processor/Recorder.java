package begyyal.splatoon.processor;

import java.io.Closeable;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import begyyal.commons.util.function.ThreadController;
import begyyal.commons.util.object.SuperList;
import begyyal.commons.util.object.SuperList.SuperListGen;
import begyyal.commons.util.web.constant.HttpHeader;
import begyyal.commons.util.web.constant.HttpStatus;
import begyyal.splatoon.constant.GameType;
import begyyal.splatoon.constant.IkaringApi;
import begyyal.splatoon.constant.Rule;
import begyyal.splatoon.db.ResultTableDao;
import begyyal.splatoon.gui.constant.GuiParts;
import begyyal.splatoon.object.BattleResult;
import begyyal.splatoon.object.DisplayDataBundle;
import begyyal.splatoon.object.DisplayDataBundle.PaneData;
import begyyal.splatoon.object.HttpBandle;
import begyyal.splatoon.object.ResultTable;
import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.layout.Background;

public class Recorder implements Closeable {

    private final int intervalSec;
    private final String sessionId;
    private final HttpBandle<IkaringApi> httpBandle;
    private final ResultTableDao dao;
    private final ExecutorService exe;

    private Recorder() throws IOException {
	var res = ResourceBundle.getBundle("common");
	this.intervalSec = Integer.parseInt(res.getString("pollingIntervalSec"));
	this.sessionId = res.getString("iksm");
	this.httpBandle = new HttpBandle<IkaringApi>();
	this.dao = ResultTableDao.newi();
	this.exe = Executors.newSingleThreadExecutor(
	    ThreadController.createPlainThreadFactory("spla-po"));
    }

    public static Recorder newi() throws IOException {
	return new Recorder();
    }

    public DisplayDataBundle run() throws Exception {

	this.httpBandle.setReq(IkaringApi.RESULTS, this.createReq());
	var res = this.httpBandle.send(IkaringApi.RESULTS, BodyHandlers.ofString());
	var status = HttpStatus.parse(res.statusCode());
	if (status.getCategory() != 2)
	    if (status == HttpStatus.Forbidden) {
		throw new Exception(
		    "Http response by the ikaring API shows Forbidden(403) status, "
			    + "so iksm_session may be wrong or expired.");
	    } else
		throw new Exception("Http status by the ikaring API is not success. status:" + status.code);

	var dataBundle = new DisplayDataBundle();
	this.record(res.body(), dataBundle);

	this.exe.execute(() -> {
	    while (ThreadController.sleep(1000 * intervalSec))
		this.process(dataBundle);
	});

	return dataBundle;
    }

    private void process(DisplayDataBundle dataBundle) {
	this.httpBandle.sendAsync(IkaringApi.RESULTS, BodyHandlers.ofString())
	    .thenApply(HttpResponse::body)
	    .thenAccept(j -> this.record(j, dataBundle))
	    .join();
    }

    private HttpRequest createReq() {
	return HttpRequest.newBuilder()
	    .uri(IkaringApi.RESULTS.toURI())
	    .header(HttpHeader.Cookie.str, "iksm_session=" + this.sessionId)
	    .build();
    }

    private void record(String json, DisplayDataBundle dataBundle) {

	ResultTable table = null;
	try {
	    table = this.dao.read();
	} catch (IOException e) {
	    System.out.println("[ERROR] IOException caused when the dao reading.");
	    e.printStackTrace();
	    return;
	}

	JsonNode tree = null;
	try {
	    tree = new ObjectMapper().readTree(json);
	} catch (IOException e1) {
	    System.out.println("[ERROR] IOException caused when reading json.");
	    e1.printStackTrace();
	    return;
	}
	var list = SuperListGen.<BattleResult>newi();

	for (JsonNode jn : tree.get("results")) {
	    var type = GameType.parse(jn.get("type").asText());
	    if (type == null)
		continue;
	    var rule = Rule.parse(jn.get("rule").get("key").asText());
	    if (rule == null)
		continue;
	    var battleNum = jn.get("battle_number").asInt();
	    var isWin = "victory".equals(jn.get("my_team_result").get("key").asText());
	    int weaponId = jn.get("player_result").get("player").get("weapon").get("id").asInt();
	    list.add(new BattleResult(battleNum, isWin, type, rule, weaponId));
	}

	if (!table.integrate(list.reverse()) && !dataBundle.totalData.data.isEmpty())
	    return;

	for (var t : GameType.values())
	    this.fillPaneData(
		dataBundle.dataByType.get(t),
		table.getWinRates(t),
		table.getTruncationRange(t));
	for (var r : Rule.values())
	    this.fillPaneData(
		dataBundle.dataByRule.get(r),
		table.getWinRates(r),
		table.getTruncationRange(r));
	this.fillPaneData(
	    dataBundle.totalData,
	    table.getTotalWinRates(),
	    table.getTotalTruncationRange());

	try {
	    this.dao.write(table);
	} catch (IOException e) {
	    System.out.println("[ERROR] IOException caused when the dao writing.");
	    e.printStackTrace();
	    return;
	}
    }

    private void fillPaneData(
	PaneData pd,
	SuperList<Integer> winRates,
	SuperList<Boolean> ppreBase) {
	var newData = IntStream.range(-winRates.size() + 1, 1)
	    .mapToObj(i -> this.createDataPoint(i, winRates.next()))
	    .collect(Collectors.toList());
	var newPpre = ppreBase.stream()
	    .map(this::distinguishBkg)
	    .collect(Collectors.toList());
	if (pd.data.isEmpty()) {
	    pd.data.setAll(newData);
	    pd.ppre.setAll(newPpre);
	} else
	    Platform.runLater(() -> {
		pd.data.setAll(newData);
		pd.ppre.setAll(newPpre);
	    });
    }

    private Data<Number, Number> createDataPoint(int x, int y) {
	return new XYChart.Data<Number, Number>(x, y);
    }

    private Background distinguishBkg(Boolean isWin) {
	return isWin == null ? GuiParts.bkgWhite : isWin ? GuiParts.bkgRed : GuiParts.bkgBlue;
    }

    @Override
    public void close() throws IOException {
	this.exe.shutdown();
	this.exe.shutdownNow();
    }
}
