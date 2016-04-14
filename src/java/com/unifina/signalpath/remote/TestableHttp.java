package com.unifina.signalpath.remote;

import org.apache.http.nio.client.HttpAsyncClient;

/**
 * Simple holder for mock httpClient
 * This class must be here (and not e.g. in test/unit/com/unifina/signalpath/remote)
 * 	so that the deserializer / class-loader finds it
 * @see HttpSpec where this class is used
 */
class TestableHttp extends Http {
	public transient static HttpAsyncClient httpClient;

	@Override
	protected HttpAsyncClient getHttpClient() {
		return httpClient;
	}
}