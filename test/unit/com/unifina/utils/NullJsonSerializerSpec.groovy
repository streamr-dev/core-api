package com.unifina.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.codehaus.groovy.grails.web.json.JSONObject
import spock.lang.Specification

class NullJsonSerializerSpec extends Specification {
	Gson gson = new GsonBuilder()
		.registerTypeAdapter(JSONObject.Null, new NullJsonSerializer())
		.serializeNulls()
		.create()

	void "converts JSONObject.Null to null"() {
		setup:
		JSONObject object = new JSONObject()
		object.put("name", JSONObject.NULL)

		when:
		String s = gson.toJson(object)
		// This fails:
		// String s = new JsonBuilder(object).toString()

		then:
		s == '{"name":null}'
	}
}
