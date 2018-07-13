package com.unifina.api.node;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;

public class NodeRequest {
	private final URL url;
	private final HttpServletRequest request;
	private final HttpServletResponse response;

	public NodeRequest(String nodeAddress, String path, HttpServletRequest request, HttpServletResponse response)
			throws MalformedURLException {
		this.url = new URL(request.getScheme()
				+ "://"
				+ nodeAddress
				+ ":"
				+ request.getServerPort()
				+ path
				+ (request.getQueryString() == null ? "" : "?" + request.getQueryString()));
		this.request = request;
		this.response = response;
	}

	public URL getUrl() {
		return url;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}
}
