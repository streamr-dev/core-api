package com.unifina.feed.map;

import com.unifina.feed.MessageParser;
import grails.converters.JSON;

import java.util.Date;
import java.util.Map;

public class JSONMessageParser implements MessageParser<Object, MapMessage> {
	
	@Override
	public MapMessage parse(Object raw) {
		String s = raw.toString();
		Map json = (Map) JSON.parse(s);
		return new MapMessage(inferTimestamp(json), json);
	}

	private static Date inferTimestamp(Map json) {
		return json.containsKey("timestamp") ? new Date((long) json.get("timestamp")) : new Date();
	}
}
