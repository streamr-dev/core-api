package com.streamr.core.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.codehaus.groovy.grails.web.json.JSONObject;

import java.lang.reflect.Type;

public class NullJsonSerializer implements JsonSerializer<JSONObject.Null> {
	@Override
	public JsonElement serialize(JSONObject.Null object, Type type, JsonSerializationContext context) {
		return null;
	}
}
