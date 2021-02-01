package com.unifina.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.codehaus.groovy.grails.web.json.JSONObject;

public class JSONUtil {
	private static GsonBuilder createBuilder() {
		GsonBuilder builder = new GsonBuilder()
				.serializeNulls()
				.registerTypeAdapter(JSONObject.Null.class, new NullJsonSerializer())
				.setDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
		return builder;
	}

	public static Gson createGsonBuilder() {
		GsonBuilder builder = createBuilder();
		Gson gson = builder.create();
		return gson;
	}

	public static Gson createPrettyPrintingGsonBuilder() {
		GsonBuilder builder = createBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		return gson;
	}
}
