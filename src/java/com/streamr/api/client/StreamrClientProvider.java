package com.streamr.api.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
}
