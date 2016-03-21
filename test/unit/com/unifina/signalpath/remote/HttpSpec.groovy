package com.unifina.signalpath.remote

import com.unifina.utils.testutils.ModuleTestHelper
import groovy.json.JsonBuilder
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import spock.lang.Specification

class HttpSpec extends Specification {
	Http module

	def setup() {
		module = new TestableHttp()
		module.init()

		// inject our own HTTP client that always responds with mocked response function
		TestableHttp.httpClient = new DefaultHttpClient() {
			@Override
			CloseableHttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
				return getResponseStub(request)
			}
		}
	}

	/** Override response function to provide the mock server implementation */
	def response = { request -> [] }

	/** "Implement" CloseableHttpResponse interface. No extra classes needed, hooray for stubbing */
	def getResponseStub(HttpUriRequest request) {
		return Stub(CloseableHttpResponse) {
			getEntity() >> new StringEntity(new JsonBuilder(response(request)).toString())
		}
	}

	void "no input, no response"() {
		when:
		def inputValues = [trigger: [1, true, "test"]]
		def outputValues = [error: [null, null, null]]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "no input, constant response (ignored)"() {
		when:
		def inputValues = [trigger: [1, true, "test"]]
		def outputValues = [error: [null, null, null]]
		response = { request -> [foo: 3] }
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "no input, constant response"() {
		when:
		module.configure([
			options: [inputCount: [value: 0], outputCount: [value: 1]],
			outputs: [[name: "out1", displayName: "foo"]]
		])
		def inputValues = [trigger: [1, true, "test"]]
		def outputValues = [error: [null, null, null], out1: [3, 3, 3]]
		response = { request -> [foo: 3] }
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "one input, empty response"() {
		when:
		module.configure([options: [inputCount: [value: 1], outputCount: [value: 0]]])
		def inputValues = [trigger: [1, true, "test"], in1: [4, 20, "everyday"]]
		def outputValues = [error: [null, null, null]]
		response = { request -> [] }
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "one input, constant response"() {
		when:
		module.configure([
			options: [inputCount: [value: 1], outputCount: [value: 1]],
			outputs: [[name: "out1", displayName: "foo"]]
		])
		def inputValues = [trigger: [1, true, "test"], in1: [4, 20, "everyday"]]
		def outputValues = [error: [null, null, null], out1: [3, 3, 3]]
		response = { request -> [foo: 3] }
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	/*
	void "test GET parameters"() {
		when:
		module.configure([
			options: [inputCount: [value: 2], outputCount: [value: 0]],
			params: [
				[name: "URL", value: "localhost"],
				[name: "verb", value: "GET"],
				[name: "in1", displayName: "inputput", value: 123],
				[name: "in2", displayName: "nother", value: false]
			]
		])
		def inputValues = [trigger: [1, true, "test"], in1: [666, "666", 2*333], in2: [1+1==2, true, "true"]]
		def outputValues = [error: [null, null, null]]
		response = { HttpUriRequest request ->
			assert request.URI == "localhost?inputput=666&nother=true"
		}
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
	*/

	void "empty Http request object"() {
		when:
		//module.configure([inputCount: 1, outputCount: 1])
		//module.getInput("URL").receive("localhost")
		//module.getInput("verb").receive("POST")
		def inputValues = [trigger: [1]]
		def outputValues = [error: [null]]
		//http.response = { request -> [foo: 3] }

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			//.extraIterationsAfterInput(1)
			.test()
	}

}
