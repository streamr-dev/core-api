package com.unifina.controller

import com.unifina.service.ApiError
import com.unifina.service.ApiException
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(ErrorController)
class ErrorControllerSpec extends Specification {

	void "Generic exception rendering"() {
		when:
		request.exception = new Exception(new RuntimeException("test message"))
		controller.index()

		then:
		response.status == 500
		response.json == [code: "RuntimeException", message: "test message"]
	}

	void "APIExceptions must be rendered using the status code and string code provided"() {
		when:
		request.exception = new Exception(new ApiException(404, "TEST_CODE", "test message"))
		controller.index()

		then:
		response.status == 404
		response.json == [code: "TEST_CODE", message: "test message"]
	}

	void "Must catch exceptions that occur during rendering of an error"() {
		when:
		request.exception = new Exception(new ThrowingApiException(500, "CODE", "MESSAGE"))
		controller.index()

		then:
		response.status == 500
		response.json.code == "ERROR_WHILE_RENDERING_EXCEPTION"
	}

	class ThrowingApiException extends ApiException {

		ThrowingApiException(int statusCode, String code, String message) {
			super(statusCode, code, message)
		}

		@Override
		ApiError asApiError() {
			throw new RuntimeException("Test")
		}
	}

}
