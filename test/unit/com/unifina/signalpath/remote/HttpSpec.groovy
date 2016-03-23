package com.unifina.signalpath.remote

import com.unifina.utils.testutils.ModuleTestHelper
import groovy.json.JsonBuilder
import org.apache.http.Header
import org.apache.http.HttpEntity
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.HttpClient
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
		// TestableHttp is Http module wrapped so that we can inject our own mock HttpClient
		// Separate class is needed in same path as Http.java; anonymous class won't work with de-serializer
		TestableHttp.httpClient = mockClient
		module = new TestableHttp()
		module.init()
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
			}
		}
	}

	void "no input, no response"() {
		def inputValues = [trigger: [1, true, "test"]]
		def outputValues = [error: [null, null, null]]
		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "no input, unexpected object response (ignored)"() {
		def inputValues = [trigger: [1, true, "test"]]
		def outputValues = [error: [null, null, null]]
		response = [foo: 3, bar: 2, shutdown: "now"]
		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "no input, object response"() {
		module.configure([
			options: [inputCount: [value: 0], outputCount: [value: 1]],
			outputs: [[name: "out1", displayName: "foo"]]
		])
		def inputValues = [trigger: [1, true, "test"]]
		def outputValues = [error: [null, null, null], out1: [3, 3, 3]]
		response = [foo: 3]
		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "empty response"() {
		module.configure([options: [inputCount: [value: 1], outputCount: [value: 1]]])
		def inputValues = [trigger: [1, true, "test"], in1: [4, 20, "everyday"]]
		def outputValues = [error: [null, null, null]]
		response = []
		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "one input, echo response (just value, no JSON object wrapper)"() {
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
		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "two inputs, three outputs, array constant response with too many elements (ignored)"() {
		module.configure([
			options: [inputCount: [value: 2], outputCount: [value: 3]],
			inputs : [[name: "in1", displayName: "hark"], [name: "in2", displayName: "snark"]]
		])
		def inputValues = [trigger: [1, true, "test"], in1: [4, 20, "everyday"], in2: [1, 2, "ree"]]
		def outputValues = [error: [null, null, null], out1: [true, true, true],
							out2 : ["developers", "developers", "developers"], out3: [1, 1, 1]]
		response = { request -> [true, "developers", 1, 2, 3, 4] }
		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "two inputs, three outputs, array varying response with too few elements"() {
		module.configure([
			options: [inputCount: [value: 2], outputCount: [value: 3]],
			inputs : [[name: "in1", displayName: "hark"], [name: "in2", displayName: "snark"]]
		])
		def inputValues = [trigger: [1, true, "test"], in1: [4, 20, "everyday"], in2: [1, 2, "ree"]]
		def outputValues = [error: [null, null, null], out1: [":)", ":|", ":("]]
		response = [[":)"], [":|"], [":("]]
		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "two inputs, three outputs, array varying length response"() {
		module.configure([
			options: [inputCount: [value: 2], outputCount: [value: 3]],
			inputs : [[name: "in1", displayName: "hark"], [name: "in2", displayName: "snark"]]
		])
		def inputValues = [trigger: [1, true, "test"], in1: [4, 20, "everyday"], in2: [1, 2, "ree"]]
		def outputValues = [error: [null, null, null], out1: [":)", ":|", ":("],
							out2 : [null, 8, 7], out3: [null, null, 6]]
		response = [[":)"], [":|", 8], [":(", 7, 6, 5, 4, 3]]
		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "GET request generates correct URL params"() {
		module.configure([
			options: [inputCount: [value: 2], outputCount: [value: 0]],
			params : [
				[name: "URL", value: "localhost"],
				[name: "verb", value: "GET"],
				[name: "in1", displayName: "inputput", value: 123],
				[name: "in2", displayName: "nother", value: false]
			]
		])
		def inputValues = [trigger: [1, true, "test"], in1: [666, "666", 2 * 333], in2: [1 + 1 == 2, true, "true"]]
		def outputValues = [error: [null, null, null]]
		response = { HttpUriRequest request ->
			assert request.URI.toString().equals("localhost?inputput=666&nother=true")
		}
		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "HTTP request headers"() {
		def headers = [
			user  : [name: "header1", displayName: "user", value: "head"],
			token : [name: "header2", displayName: "token", value: "bang"],
			apikey: [name: "header3", displayName: "apikey", value: "er"]
		]
		module.configure([
			options: [inputCount: [value: 0], outputCount: [value: 0], headerCount: [value: headers.size()]],
			params : headers.values().toList()
		])
		def inputValues = [trigger: [1, true, "metal", 666]]
		def outputValues = [:]
		response = { HttpUriRequest request ->
			int found = 0
			request.allHeaders.each { Header h ->
				if (headers.containsKey(h.name)) {
					assert headers[h.name].value == h.value
					found++
				}
			}
			assert found == headers.size()
		}
		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "JSON object dot notation works for output parsing"() {
		module.configure([
			options: [inputCount: [value: 0], outputCount: [value: 3]],
			outputs: [
				[name: "out1", displayName: "seasons"],
				[name: "out2", displayName: "best.pony"],
				[name: "out3", displayName: "best.pals.human"]
			]
		])
		def inputValues = [trigger: [1, true, "test"]]
		def outputValues = [error: [null, null, null], out1: [4, 4, 4],
							out2: ["Pink", "Pink", "Pink"], out3: ["Finn", "Finn", "Finn"]]
		response = [best: [pony: "Pink", pals: [dog: "Jake", human: "Finn"]], seasons: 4]
		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "JSON object dot notation supports array indexing too"() {
		module.configure([
			options: [inputCount: [value: 0], outputCount: [value: 3]],
			outputs: [
				[name: "out1", displayName: "seasons[1]"],
				[name: "out2", displayName: "best.pals.count"],
				[name: "out3", displayName: "best.pals[1].name"]
			]
		])
		def inputValues = [trigger: [1, true, "test"]]
		def outputValues = [error: [null, null, null], out1: [3, 3, 3],
							out2: [2, 2, 2], out3: ["Finn", "Finn", "Finn"]]
		response = [best: [pals: [[name: "Jake", species: "Dog"], [name: "Finn", species: "Human"]]], seasons: [4,3,2,1]]
		expect:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
