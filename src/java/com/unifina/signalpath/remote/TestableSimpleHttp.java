package com.unifina.signalpath.remote;

import org.apache.http.client.HttpClient;

/**
 * Simple holder for mock httpClient
 * This class must be here (and not e.g. in test/unit/com/unifina/signalpath/remote)
 * 	so that the deserializer / class-loader finds it
 * @see SimpleHttpSpec where this class is used
 */
class TestableSimpleHttp extends SimpleHttp {
	public transient static HttpClient httpClient;
	@Override
	protected HttpClient getHttpClient() {
		return httpClient;
	}
}