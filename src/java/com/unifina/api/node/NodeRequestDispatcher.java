package com.unifina.api.node;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class NodeRequestDispatcher {
	private static final Logger log = Logger.getLogger(NodeRequest.class);

	private static final List<String> HTTP_VERBS_WITH_BODY = Arrays.asList("POST", "PUT");

	// Adapted from https://stackoverflow.com/questions/12130992/forward-httpservletrequest-to-a-different-server
	public void perform(NodeRequest nodeRequest) throws IOException {
		log.info("Performing node redirection to " + nodeRequest.getUrl());

		boolean hasBody = HTTP_VERBS_WITH_BODY.contains(nodeRequest.getRequest().getMethod().toUpperCase());

		HttpURLConnection conn = (HttpURLConnection) nodeRequest.getUrl().openConnection();
		conn.setRequestMethod(nodeRequest.getRequest().getMethod());

		// Copy headers
		Enumeration<String> headers = nodeRequest.getRequest().getHeaderNames();
		while (headers.hasMoreElements()) {
			String header = headers.nextElement();
			Enumeration<String> values = nodeRequest.getRequest().getHeaders(header);
			while (values.hasMoreElements()) {
				String value = values.nextElement();
				conn.addRequestProperty(header, value);
			}
		}

		conn.setUseCaches(false);
		conn.setDoInput(true);
		conn.setDoOutput(hasBody);
		conn.connect();

		// Write body
		final byte[] buffer = new byte[16384];
		while (hasBody) {
			int read = nodeRequest.getRequest().getInputStream().read(buffer);
			if (read <= 0) {
				break;
			}
			conn.getOutputStream().write(buffer, 0, read);
		}

		// Copy response headers
		nodeRequest.getResponse().setStatus(conn.getResponseCode());
		for (int i = 0; ; ++i) {
			String header = conn.getHeaderFieldKey(i);
			if (header == null) {
				break;
			}

			String value = conn.getHeaderField(i);
			nodeRequest.getResponse().setHeader(header, value);
		}

		// Copy response body
		while (true) {
			final int read = conn.getInputStream().read(buffer);
			if (read <= 0) break;
			nodeRequest.getResponse().getOutputStream().write(buffer, 0, read);
		}
	}
}
