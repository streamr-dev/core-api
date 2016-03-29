package com.unifina.signalpath.remote

import com.unifina.utils.testutils.ModuleTestHelper
import groovy.json.JsonBuilder
import org.apache.http.Header
import org.apache.http.StatusLine
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.StringEntity
import org.apache.http.util.EntityUtils
import org.json.JSONObject
import spock.lang.Specification

class HttpSpec extends Specification {
	Http module

	/**
	 * Override "response" to provide the mock server implementation
	 * If closure, will be executed (argument is HttpUriRequest)
	 * If constant, will be returned
	 * If array, elements will be returned in sequence (closures executed, cyclically repeated if too short)
	 * If you want to return an array,
	 *   use closure that returns an array (see default below)
	 *   or array of arrays
	 */
	def response = { request -> [] }

	def setup() {
		// TestableSimpleHttp is SimpleHttp module wrapped so that we can inject our own mock HttpClient
		// Separate class is needed in same path as SimpleHttp.java; anonymous class won't work with de-serializer
		TestableHttp.httpClient = mockClient
		module = new TestableHttp()
		module.init()
		module.configure([
			params : [
				[name: "URL", value: "localhost"],
				[name: "verb", value: "GET"],
			]
		])
	}

	/** HttpClient that generates mock responses to HttpUriRequests according to this.response */
	def mockClient = Stub(HttpClient) {
		def responseI = [].iterator()
		execute(_) >> { HttpUriRequest request ->
			Stub(CloseableHttpResponse) {
				getEntity() >> {
					def ret = response
					// array => iterate
					if (ret instanceof Iterable) {
						// end of array -> restart from beginning
						if (!responseI.hasNext()) {
							responseI = response.iterator()
						}
						ret = responseI.hasNext() ? responseI.next() : []
					}
					// closure => execute
					if (ret instanceof Closure) {
						ret = ret(request)
					}
					// wrap in JSON and HttpEntity
					return new StringEntity(new JsonBuilder(ret).toString())
				}
				getStatusLine() >> Stub(StatusLine) {
					getStatusCode() >> 200
				}
				getAllHeaders() >> [Stub(Header) {
					getName() >> "x-unit-test"
					getValue() >> "testing123"
				}]
			}
		}
	}

	void "no input, constant response"() {
		def inputValues = [
			params: [[:], [:], [:]],
			headers: [[:], [:], [:]],
			body: [[:], [:], [:]]
		]
		def outputValues = [
			errors: [null, null, null],
			data: [[test: 1], [test: 1], [test: 1]],
			"status code": [200d, 200d, 200d],
			//ping: [0, 0, 0],
			headers: [["x-unit-test": "testing123"], ["x-unit-test": "testing123"], ["x-unit-test": "testing123"]]
		]
		response = [test: 1]
		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
