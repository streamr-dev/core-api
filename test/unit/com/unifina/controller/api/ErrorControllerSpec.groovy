package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.api.ValidationException
import com.unifina.exceptions.CanvasUnreachableException
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(ErrorController)
class ErrorControllerSpec extends Specification {

	void "Generic exception rendering"() {
		when:
		request.exception = new Exception(new RuntimeException("test message"))
		request.isApiAction = true
		controller.index()

		then:
		response.status == 500
		response.json == [code: "RuntimeException", message: "test message"]
	}

	void "APIExceptions must be rendered using the status code and string code provided"() {
		when:
		request.exception = new Exception(new ApiException(404, "TEST_CODE", "test message"))
		request.isApiAction = true
		controller.index()

		then:
		response.status == 404
		response.json == [code: "TEST_CODE", message: "test message"]
	}

	void "Some exceptions can have special rendering"() {
		when:
		request.exception = new Exception(new CanvasUnreachableException())
		request.isApiAction = true
		controller.index()

		then:
		response.status == 500
		response.json.code == "CANVAS_UNREACHABLE"
	}

}
