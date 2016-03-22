package com.unifina.signalpath.remote

import com.unifina.utils.testutils.ModuleTestHelper
import groovy.json.JsonBuilder
import org.apache.http.HttpEntity
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.json.JSONObject
import spock.lang.Specification

class HttpSpec extends Specification {
	Http module

	/**
	 * Override response to provide the mock server implementation
	 * If closure, will be executed (argument is HttpUriRequest)
	 * If constant, will be returned
	 * If array, elements will be returned in sequence (closures executed)
	 * If you want to return an array,
	 *   use closure that returns an array (see default below)
	 *   or array of arrays
	 */
	def response = { request -> [] }

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

	/** "Implement" CloseableHttpResponse interface. No extra classes needed, hooray for stubbing */
	def getResponseStub(HttpUriRequest request) {
		return Stub(CloseableHttpResponse) {
			getEntity() >> simulateServer(request)
		}
	}

	/** generate the mock responses to requests according to this.response */
	private HttpEntity simulateServer(HttpUriRequest request) {
		def ret = response
		// array => iterate
		if (ret instanceof Iterable) {
			if (!serverI.hasNext()) {
				serverI = response.iterator()
			}
			ret = serverI.hasNext() ? serverI.next() : []
		}
		// closure => execute
		if (ret instanceof Closure) {
			ret = ret(request)
		}
		// wrap in JSON and HttpEntity
		return new StringEntity(new JsonBuilder(ret).toString())
	}
	def serverI = [].iterator()


	void "no input, no response"() {
		when:
		def inputValues = [trigger: [1, true, "test"]]
		def outputValues = [error: [null, null, null]]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "no input, unexpected object response (ignored)"() {
		when:
		def inputValues = [trigger: [1, true, "test"]]
		def outputValues = [error: [null, null, null]]
		response = [foo: 3, bar: 2, shutdown: "now"]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "no input, object response"() {
		when:
		module.configure([
			options: [inputCount: [value: 0], outputCount: [value: 1]],
			outputs: [[name: "out1", displayName: "foo"]]
		])
		def inputValues = [trigger: [1, true, "test"]]
		def outputValues = [error: [null, null, null], out1: [3, 3, 3]]
		response = [foo: 3]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "empty response"() {
		when:
		module.configure([options: [inputCount: [value: 1], outputCount: [value: 1]]])
		def inputValues = [trigger: [1, true, "test"], in1: [4, 20, "everyday"]]
		def outputValues = [error: [null, null, null]]
		response = []
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "one input, identity function response"() {
		when:
		module.configure([
			options: [inputCount: [value: 1], outputCount: [value: 1]],
			outputs: [[name: "out1", displayName: "foo"]]
		])
		def messages = ["4", "20", "everyday"]
		def inputValues = [trigger: [1, true, "test"], in1: messages]
		def outputValues = [error: [null, null, null], out1: messages]
		response = { HttpPost r ->
			def jsonString = EntityUtils.toString(r.entity)
			def ob = new JSONObject(jsonString)
			return ob.get("in1")
		}
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "two inputs, three outputs, array constant response with too many elements (ignored)"() {
		when:
		module.configure([
				options: [inputCount: [value: 2], outputCount: [value: 3]],
				inputs: [[name: "in1", displayName: "hark"], [name: "in2", displayName: "snark"]]
		])
		def inputValues = [trigger: [1, true, "test"], in1: [4, 20, "everyday"], in2: [1, 2, "ree"]]
		def outputValues = [error: [null, null, null], out1: [true, true, true],
							out2: ["developers", "developers", "developers"], out3: [1, 1, 1]]
		response = { request -> [true, "developers", 1, 2, 3, 4] }
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "two inputs, three outputs, array varying response with too few elements"() {
		when:
		module.configure([
				options: [inputCount: [value: 2], outputCount: [value: 3]],
				inputs: [[name: "in1", displayName: "hark"], [name: "in2", displayName: "snark"]]
		])
		def inputValues = [trigger: [1, true, "test"], in1: [4, 20, "everyday"], in2: [1, 2, "ree"]]
		def outputValues = [error: [null, null, null], out1: [":)", ":|", ":("]]
		response = [[":)"], [":|"], [":("]]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "two inputs, three outputs, array varying length response"() {
		when:
		module.configure([
				options: [inputCount: [value: 2], outputCount: [value: 3]],
				inputs: [[name: "in1", displayName: "hark"], [name: "in2", displayName: "snark"]]
		])
		def inputValues = [trigger: [1, true, "test"], in1: [4, 20, "everyday"], in2: [1, 2, "ree"]]
		def outputValues = [error: [null, null, null], out1: [":)", ":|", ":("],
							out2: [null, 8, 7], out3: [null, null, 6]]
		response = [[":)"], [":|", 8], [":(", 7, 6, 5, 4, 3]]
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

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
			assert request.URI.toString().equals("localhost?inputput=666&nother=true")
		}
		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
