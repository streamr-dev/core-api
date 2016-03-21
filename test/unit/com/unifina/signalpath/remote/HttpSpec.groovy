package com.unifina.signalpath.remote

import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class HttpSpec extends Specification {

	Http module;
	DummyHttpRequest request;
	DummyHttpResponse response;

	def setup() {
		module = new Http()
		module.init()

		//Unirest.get =

		MetaClass mc = Unirest["metaClass"]		// calls Unirest.get ...ouch.

		Unirest.metaClass = [
			static: [
				get: { url -> request },
				post: { url -> request },
			]
		]
	}

	class DummyHttpRequest {
		def body = { String s -> null }
		def header = { String key, String value -> null }
		def asJson = { -> response }
	}

	class DummyHttpResponse {
		def json = "{}"
		def getBody = { -> new JsonNode(json) }
	}

	void "empty Http request object"() {
		when:
		module.getInput("URL").receive("http://localhost/")
		def inputValues = [:]
		def outputValues = [:]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
