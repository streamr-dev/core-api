package com.unifina.feed.kafka;

import com.unifina.feed.StreamrMessage;
import grails.converters.JSON;

import java.util.Date;
import java.util.Map;

import com.unifina.feed.MessageParser;
import com.unifina.kafkaclient.UnifinaKafkaMessage;

public class KafkaMessageParser implements MessageParser<UnifinaKafkaMessage, StreamrMessage> {

	@Override
	public StreamrMessage parse(UnifinaKafkaMessage raw) {
		if (raw.getContentType()==UnifinaKafkaMessage.CONTENT_TYPE_JSON) {
			String s = raw.toString();
			Map json = (Map) JSON.parse(s);
			return new StreamrMessage(raw.getChannel(), new Date(raw.getTimestamp()), new Date(), json);
		}
		else throw new RuntimeException("Unknown content type: "+raw.getContentType());
	}

}
