package com.streamr.api.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

public class StreamrClientProvider implements StreamrClient {
	// webTarget is the base URL for Streamr REST API service derived from baseUrl (without trailing slash).
	private final WebTarget webTarget;

	// client is a Jersey HTTP REST API client for accessing streamr.com.
	private final Client client;

	public StreamrClientProvider(final String baseUrl) {
		this.client = ClientBuilder.newClient();
		this.webTarget = this.client.target(baseUrl);
	}

	/**
	 * @param token is the value of HTTP Authorization header. For example: "token value-of-token".
	 */
	@Override
	public CanvasesPerNode canvasesPerNode(final String token, final String nodeIp) {
		final WebTarget target = webTarget.path(String.format("/nodes/%s/canvases", nodeIp));
		final Response res = target.request(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token).get();
		final CanvasesPerNode result = res.readEntity(CanvasesPerNode.class);
		return result;
	}

	/**
	 * @param token is the value of HTTP Authorization header. For example: "token value-of-token".
	 */
	@Override
	public List<Map<String, Object>> shutdown(final String token, final String nodeIp) {
		final WebTarget target = webTarget.path(String.format("/nodes/%s/shutdown", nodeIp));
		final Entity<String> input = Entity.json("");
		final GenericType<List<Map<String, Object>>> resultType = new GenericType<List<Map<String, Object>>>() {};
		final List<Map<String, Object>> res = target.request(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token).post(input, resultType);
		return res;
	}

	public static void main(final String... args) {
		StreamrClient client = new StreamrClientProvider("http://localhost:8081/streamr-core/api/v1");
		final CanvasesPerNode canvases = client.canvasesPerNode("token tester-admin-api-key", "192.168.10.116");
		System.out.println(canvases.shouldBeRunning.get(0));
	}
}
