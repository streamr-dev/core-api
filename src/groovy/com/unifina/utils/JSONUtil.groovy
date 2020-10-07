package com.unifina.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder

import java.text.DateFormat

class JSONUtil {
	static Gson createGsonBuilder() {
		// Use Gson instead of Grails "as JSON" converter because there's no easy way to get that working in func tests that want to produce data to Streams
		Gson gson = new GsonBuilder()
			.serializeNulls()
			.setDateFormat(DateFormat.LONG)
			.create()
		return gson
	}
}
