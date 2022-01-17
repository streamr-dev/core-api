package com.unifina.controller

import com.unifina.service.ApiError
import com.unifina.service.ApiException
import grails.converters.JSON
import groovy.transform.CompileStatic

import javax.servlet.http.HttpServletResponse

class ErrorController {
	static final Map<String, Closure<ApiError>> ERROR_MAPPINGS = [
		(grails.validation.ValidationException): { grails.validation.ValidationException e -> new ApiError(422, "VALIDATION_ERROR", v.message) }
	]

	@StreamrApi(authenticationLevel = AuthLevel.NONE, expectedContentTypes = ["application/json", "multipart/form-data"])
	def index() {
		try {
			Exception exception = request.exception.cause ?: request.exception
			renderAsJson(exception)
		} catch (Exception e) {
			// Avoid infinite loop by catching any "error while showing error" -type of situation
			log.error("Failed to render exception!", e)
			response.status = 500
			render([code: "ERROR_WHILE_RENDERING_EXCEPTION", message: "An error occurred while rendering exception"] as JSON)
		}
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def notFound() {
		response.sendError(HttpServletResponse.SC_NOT_FOUND)
		return
	}

	@CompileStatic
	def renderAsJson(Exception exception) {
		def mapper = ERROR_MAPPINGS[exception.class.name]

		ApiError apiError
		if (mapper) {
			apiError = mapper.call(exception)
		} else if (exception instanceof ApiException) {
			apiError = ((ApiException) exception).asApiError()
		} else {
			apiError = new ApiError(500, exception.class.simpleName, exception.getMessage())
		}

		// Log internal errors
		if (apiError.statusCode >= 500 && apiError.statusCode < 600) {
			String uri = request.getHeader("X-Request-URI")
			if (!uri) {
				uri = request.getRequestURL().toString()
			}
			log.error("Unexpected error occurred on request to ${request.getMethod()} ${uri}, returning status code 500", exception)
		}

		def extraHeaders = apiError.getHeaders()
		if (extraHeaders != null) {
			extraHeaders.each { key, value ->
				response.setHeader(key, value)
			}
		}

		response.status = apiError.statusCode
		render(apiError.toMap() as JSON)
	}
}
