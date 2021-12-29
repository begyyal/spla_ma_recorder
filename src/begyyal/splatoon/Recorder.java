package begyyal.splatoon;

import java.io.Closeable;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Calendar;
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
import begyyal.splatoon.object.BattleResult;
import begyyal.splatoon.object.DisplayDataBundle;
import begyyal.splatoon.object.ResultTable;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;

public class Recorder implements Closeable {

    private static final int intervalMin = 5;

    private final String sessionId;
    private final HttpClient client;
    private final ResultTableDao dao;
    private final ExecutorService exe;

    private Recorder() throws IOException {
	this.sessionId = ResourceBundle.getBundle("common").getString("iksm");
	this.client = HttpClient.newHttpClient();
	this.dao = ResultTableDao.newi();
	this.exe = Executors.newSingleThreadExecutor(
	    ThreadController.createPlainThreadFactory("spla-po"));
    }

    public static Recorder newi() throws IOException {
	return new Recorder();
    }

    public DisplayDataBundle run() throws Exception {

	var request = this.createReq();
	var res = client.send(request, BodyHandlers.ofString());
	var status = HttpStatus.parse(res.statusCode());
	if (status.getCategory() != 2)
	    if (status == HttpStatus.Unauthorized) {
		throw new Exception(
		    "Http response by the ikaring API shows unauthorized(401) status, "
			    + "so iksm_session may be wrong.");
	    } else
		throw new Exception("Http status by the ikaring API is not success.");

	var dataBundle = new DisplayDataBundle();
	this.record(res.body(), dataBundle);

	this.exe.execute(() -> {
	    while (true) {
		if (!ThreadController.sleep(1000 * 59l))
		    break;
		var cal = Calendar.getInstance();
		if (cal.get(Calendar.MINUTE) % intervalMin == 0)
		    this.process(dataBundle);
	    }
	});

	return dataBundle;
    }

    private void process(DisplayDataBundle dataBundle) {
	var request = this.createReq();
	client.sendAsync(request, BodyHandlers.ofString())
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
	    var battleNum = jn.get("battle_number").asInt();
	    var isWin = "victory".equals(jn.get("my_team_result").get("key").asText());
	    list.add(new BattleResult(battleNum, isWin, type));
	}

	if (!table.integrate(list.reverse()) && !dataBundle.data.isEmpty())
	    return;

	for (var t : GameType.values())
	    this.fillChartData(dataBundle.getDataByType(t), table.getWinRates(t));
	this.fillChartData(dataBundle.data, table.getTotalWinRates());

	try {
	    this.dao.write(table);
	} catch (IOException e) {
	    System.out.println("[ERROR] IOException caused when the dao writing.");
	    e.printStackTrace();
	    return;
	}
    }

    private void fillChartData(
	ObservableList<Data<Number, Number>> chartData,
	SuperList<Integer> winRates) {
	var newData = IntStream.range(-winRates.size() + 1, 1)
	    .mapToObj(i -> this.createDataPoint(i, winRates.next()))
	    .collect(Collectors.toList());
	if (chartData.isEmpty())
	    chartData.setAll(newData);
	else
	    Platform.runLater(() -> chartData.setAll(newData));
    }

    private Data<Number, Number> createDataPoint(int x, int y) {
	return new XYChart.Data<Number, Number>(x, y);
    }

    @Override
    public void close() throws IOException {
	this.exe.shutdown();
	this.exe.shutdownNow();
    }

}
