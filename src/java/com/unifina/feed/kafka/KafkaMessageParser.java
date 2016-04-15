package com.unifina.feed.kafka;

import grails.converters.JSON;

import java.util.Date;
import java.util.Map;

import com.unifina.feed.MessageParser;
import com.unifina.kafkaclient.UnifinaKafkaMessage;

public class KafkaMessageParser implements MessageParser<UnifinaKafkaMessage, KafkaMessage> {

	@Override
	public KafkaMessage parse(UnifinaKafkaMessage raw) {
		if (raw.getContentType()==UnifinaKafkaMessage.CONTENT_TYPE_JSON) {
			String s = raw.toString();
			Map json = (Map) JSON.parse(s);
			return new KafkaMessage(raw.getChannel(), new Date(raw.getTimestamp()), new Date(), json);
		}
		else throw new RuntimeException("Unknown content type: "+raw.getContentType());
	}

}
