package com.unifina.feed.map;

import grails.converters.JSON;

import java.util.Date;
import java.util.Map;

import com.unifina.feed.MessageParser;

public class JSONMessageParser implements MessageParser<Object, MapMessage> {
	
	@Override
	public MapMessage parse(Object raw) {
		String s = raw.toString();
		Map json = (Map) JSON.parse(s);
		return new MapMessage(json.containsKey("timestamp") ? new Date((long)json.get("timestamp")) : new Date(), new Date(), json);
	}

}
