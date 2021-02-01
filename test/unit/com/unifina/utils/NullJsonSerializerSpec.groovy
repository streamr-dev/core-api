package com.unifina.utils

import com.google.gson.Gson
import org.codehaus.groovy.grails.web.json.JSONObject
import spock.lang.Specification
import spock.lang.Unroll

class NullJsonSerializerSpec extends Specification {
	Gson gson = JSONUtil.createGsonBuilder()

	@Unroll
	void "converts #name to null"(String name, Object value) {
		// Grails JsonBuilder is buggy
		// "{}" == new JsonBuilder(value).toString()

		expect:
		JSONObject object = new JSONObject()
		object.put("name", value)
		String s = gson.toJson(object)

		s == '{"name":null}'

		where:
		name | value
		"JSONObject.NULL" | JSONObject.NULL
		"new JSONObject.Null()" | new JSONObject.Null()
	}
}
