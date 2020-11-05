package com.unifina.controller

import javax.servlet.http.HttpServletResponse

trait SearchHeaderLink {
	void addPaginationLinkToHeader(HttpServletResponse response, String link) {
		if (link != null) {
			response.addHeader("Link", link)
		}
	}
}
