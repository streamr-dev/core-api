package com.unifina.signalpath;

import grails.converters.JSON;
import org.codehaus.groovy.grails.web.json.JSONObject;

import java.util.Map;

public class MapParameter extends Parameter<Map> {

	public MapParameter(AbstractSignalPathModule owner, String name, Map<String, Object> defaultValue) {
		super(owner, name, defaultValue, "Map");
	}

	@Override
	public Map parseValue(String s) {
		try {
			return (JSONObject) JSON.parse(s);
		} catch (Exception e) {
			throw new RuntimeException("Invalid map parameter representation: "+s, e);
		}
	}

	@Override
	protected boolean isEmpty(Map value) {
		return super.isEmpty(value) || (value!=null && value.isEmpty());
	}
}
