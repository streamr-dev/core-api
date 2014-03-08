package com.unifina.feed.kafka;

import grails.converters.JSON;

import java.util.Map;

import kafka.serializer.StringDecoder;

import com.unifina.feed.MessageParser;

public class KafkaMessageParser implements MessageParser<RawKafkaMessage, KafkaMessage> {

	StringDecoder dec = new StringDecoder(null);
	
	@Override
	public KafkaMessage parse(RawKafkaMessage raw) {
		String s = dec.fromBytes(raw.content);
		Map json = (Map) JSON.parse(s);
		return new KafkaMessage(raw.topic, raw.receiveTime, raw.receiveTime, json);
	}

}
