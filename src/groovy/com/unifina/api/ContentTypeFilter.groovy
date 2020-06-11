package com.unifina.api

import javax.servlet.*
import javax.servlet.http.HttpServletRequest

class ContentTypeFilter implements Filter {
	private final static String HEADER_CONTENT_TYPE = "Content-Type"
	private final static String CONTENT_TYPE_JSON = "application/json"

	@Override
	void init(FilterConfig filterConfig) throws ServletException {}

	@Override
	void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request
		if (req.method != "POST" && req.method != "PUT" && req.method != "PATCH") {
			chain.doFilter(request, response)
			return
		}
		String ct = req.getHeader(HEADER_CONTENT_TYPE)
		if (ct == null) {
			RequestWrapper r = new RequestWrapper(req)
			r.setHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
			chain.doFilter(r, response)
			return
		}
		chain.doFilter(request, response)
	}

	@Override
	void destroy() {}
}
