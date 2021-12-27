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
import begyyal.splatoon.constant.IkaringApi;
import begyyal.splatoon.object.BattleResult;
import begyyal.splatoon.object.DisplayDataBundle;
import begyyal.splatoon.object.ResultTable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
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

	var chartData = FXCollections.<Data<Number, Number>>observableArrayList();

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
	
	this.record(res.body(), chartData);

	this.exe.execute(() -> {
	    while (true) {
		if (!ThreadController.sleep(1000 * 59l))
		    break;
		var cal = Calendar.getInstance();
		if (cal.get(Calendar.MINUTE) % intervalMin == 0)
		    this.process(chartData);
	    }
	});

	return new DisplayDataBundle(chartData);
    }

    private void process(ObservableList<Data<Number, Number>> chartData) {
	var request = this.createReq();
	client.sendAsync(request, BodyHandlers.ofString())
	    .thenApply(HttpResponse::body)
	    .thenAccept(j -> this.record(j, chartData))
	    .join();
    }

    private HttpRequest createReq() {
	return HttpRequest.newBuilder()
	    .uri(IkaringApi.RESULTS.toURI())
	    .header(HttpHeader.Cookie.str, "iksm_session=" + this.sessionId)
	    .build();
    }

    private void record(String json, ObservableList<Data<Number, Number>> chartData) {

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

	// 勝率も取れるが、複数断面まとめて更新した際の補完は結局するので、単純化を意図してスルー
	for (JsonNode jn : tree.get("results")) {
	    if (!"gachi".equals(jn.get("type").asText()))
		continue;
	    var battleNum = jn.get("battle_number").asInt();
	    var isWin = "victory".equals(jn.get("my_team_result").get("key").asText());
	    list.add(new BattleResult(battleNum, isWin));
	}

	if (!table.integrate(list.reverse()) && !chartData.isEmpty())
	    return;

	this.fillChartData(chartData, table.getWinRates());

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
