package begyyal.splatoon.object;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.util.concurrent.CompletableFuture;

import begyyal.commons.util.object.SuperMap;
import begyyal.commons.util.object.SuperMap.SuperMapGen;

public class HttpBandle<T> {
    private HttpClient client;
    private SuperMap<T, HttpRequest> reqMap;

    public HttpBandle() {
	this.client = HttpClient.newHttpClient();
	this.reqMap = SuperMapGen.newi();
    }

    public void setReq(T key, HttpRequest req) {
	this.reqMap.put(key, req);
    }

    public <H> HttpResponse<H> send(T reqKey, BodyHandler<H> handler)
	throws IOException, InterruptedException {
	HttpResponse<H> res = null;
	try {
	    res = this.client.send(this.reqMap.get(reqKey), handler);
	} catch (Exception e) {
	    // OSスリープによる接続リセット対策
	    res = this.client.send(this.reqMap.get(reqKey), handler);
	}
	return res;
    }

    public <H> CompletableFuture<HttpResponse<H>> sendAsync(T reqKey, BodyHandler<H> handler) {
	return this.client.sendAsync(this.reqMap.get(reqKey), handler)
	    .exceptionallyComposeAsync(e -> {
		// OSスリープによる接続リセット対策
		return this.client.sendAsync(this.reqMap.get(reqKey), handler);
	    });
    }
}
