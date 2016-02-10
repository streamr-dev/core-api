package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.security.StreamrApi
import grails.converters.JSON
import org.codehaus.groovy.runtime.typehandling.GroovyCastException

class ErrorController {

	@StreamrApi(requiresAuthentication = false)
	def index() {
		try {
			ApiException e = request.exception.cause
			response.status = e.statusCode
			render(e.toMap() as JSON)
		} catch (GroovyCastException e) {
			response.status = 500
			render([code: "UNEXPECTED_ERROR", message: e.message] as JSON)
		}
	}
}
