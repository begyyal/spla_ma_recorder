package begyyal.splatoon.constant;

import java.net.URI;

public enum IkaringApi {
    RESULTS("https://app.splatoon2.nintendo.net/api/results");

    public final String url;

    private IkaringApi(String url) {
	this.url = url;
    }

    public URI toURI() {
	return URI.create(this.url);
    }
}
