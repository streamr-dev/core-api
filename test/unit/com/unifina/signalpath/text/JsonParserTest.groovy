package com.unifina.signalpath.text

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

class JsonParserTest extends Specification {
	JsonParser module

	void setup() {
		module = new JsonParser()
		module.init()
	}

	void "Parses correctly simple values, gives errors on malformed JSON"() {
		when:
		Map inputValues = [
			json: ["moi", "2", "[]", "{}", "[{}, {{}, [2]}]", "{"]
		]
		Map outputValues = [
			"errors": [[], [], [], [], ["org.codehaus.groovy.grails.web.json.JSONException: Expected a ':' after a key at character 9 of [{}, {{}, [2]}]"], ["org.codehaus.groovy.grails.web.json.JSONException: A JSONObject text must end with '}' at character 1 of {"]],
			"result": ["moi", 2, [], [:], [:], [:]]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}
}
