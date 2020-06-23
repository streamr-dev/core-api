package com.unifina.controller.api

import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import grails.converters.JSON

class UnitTestController {
	@StreamrApi(authenticationLevel = AuthLevel.KEY, expectedContentTypes = ["text/csv"])
	def upload() {
		Enumeration<String> headerNames = request.getHeaderNames()
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement()
			println("HTTP header: " + headerName + " = " + request.getHeader(headerName))
		}
		render([] as JSON)
	}
}
