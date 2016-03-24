package com.unifina.signalpath;

import grails.converters.JSON;
import org.codehaus.groovy.grails.web.json.JSONObject;

import java.util.Map;

public class MapParameter extends Parameter<Map<String, Object>> {

	public MapParameter(AbstractSignalPathModule owner, String name, Map<String, Object> defaultValue) {
		super(owner, name, defaultValue, "Map");
	}

	@Override
	public Map<String, Object> parseValue(String s) {
		try {
			return (JSONObject) JSON.parse(s);
		} catch (Exception e) {
			throw new RuntimeException("Invalid map parameter representation: "+s, e);
		}
	}

	@Override
	protected boolean isEmpty(Map<String, Object> value) {
		return super.isEmpty(value) || (value!=null && value.isEmpty());
	}
}
