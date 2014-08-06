package com.unifina.feed.kafka;

import grails.converters.JSON;

import java.util.Map;
import java.util.Properties;

import com.unifina.data.IStreamRequirement;
import com.unifina.domain.data.Feed;
import com.unifina.domain.data.Stream;
import com.unifina.feed.Message;
import com.unifina.feed.MessageRecipient;
import com.unifina.feed.MessageSource;
import com.unifina.kafkaclient.UnifinaKafkaConsumer;
import com.unifina.kafkaclient.UnifinaKafkaMessage;
import com.unifina.kafkaclient.UnifinaKafkaMessageHandler;
import com.unifina.utils.MapTraversal;

public class KafkaMessageSource implements MessageSource {

	private MessageRecipient recipient;

	private UnifinaKafkaConsumer consumer;
	private Feed feed;
	
	private UnifinaKafkaMessageHandler handler = new UnifinaKafkaMessageHandler() {
		
		private long offset = 0;
		
		@Override
		public void handleMessage(UnifinaKafkaMessage kafkaMessage) {
			Message msg = new Message(offset++, kafkaMessage);
			msg.checkCounter = false;
			recipient.receive(msg);
		}
	};
	
	public KafkaMessageSource(Feed feed, Map<String,Object> config) {
		this.feed = feed;
		
		Map<String,Object> kafkaConfig = MapTraversal.flatten((Map) MapTraversal.getMap(config, "unifina.kafka"));
		Properties properties = new Properties();
		for (String s : kafkaConfig.keySet())
			properties.setProperty(s, kafkaConfig.get(s).toString());
		
		consumer = new UnifinaKafkaConsumer(properties);

	}
	
	@Override
	public void setRecipient(MessageRecipient recipient) {
		this.recipient = recipient;
	}

	@Override
	public void setExpectedCounter(long expected) {

	}
	
	public void quit() {
		consumer.close();
	}

	@Override
	public void subscribe(Object subscriber) {
		Stream stream = ((IStreamRequirement) subscriber).getStream();
		Map streamConfig = (Map) JSON.parse(stream.getStreamConfig());
		consumer.subscribe(streamConfig.get("topic").toString(), handler, false);
	}
	
}
