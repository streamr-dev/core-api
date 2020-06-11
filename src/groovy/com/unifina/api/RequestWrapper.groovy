package com.unifina.api

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper

class RequestWrapper extends HttpServletRequestWrapper {
	private final Map<String, String> headers = new HashMap<String, String>()

	RequestWrapper(HttpServletRequest request) {
		super(request);
	}

	void setHeader(String name, String value) {
		headers.put(name, value)
	}

	@Override
	String getHeader(String name) {
		String value = super.getHeader(name)
		if (headers.containsKey(name)) {
			value = headers.get(name)
		}
		return value
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		List<String> names = Collections.list(super.getHeaderNames())
		for (String name : headers.keySet()) {
			names.add(name)
		}
		return Collections.enumeration(names)
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		List<String> values = Collections.list(super.getHeaders(name))
		if (headers.containsKey(name)) {
			values.add(headers.get(name))
		}
		return Collections.enumeration(values)
	}
}
