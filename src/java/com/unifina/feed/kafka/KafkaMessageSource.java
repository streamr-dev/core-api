package com.unifina.feed.kafka;

import java.util.Map;
import java.util.Properties;

import com.unifina.domain.data.Feed;
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
	
	private UnifinaKafkaMessageHandler handler = new UnifinaKafkaMessageHandler() {
		
		private long counter = 0;
		
		@Override
		public void handleMessage(UnifinaKafkaMessage kafkaMessage, String topic, int partition, long offset) {
			Message msg = new Message(kafkaMessage.getChannel(), counter++, kafkaMessage);
			msg.checkCounter = false;
			recipient.receive(msg);
		}
	};
	
	public KafkaMessageSource(Feed feed, Map<String,Object> config) {
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
	public void subscribe(Object key) {
		consumer.subscribe(key.toString(), handler);
	}

	@Override
	public void unsubscribe(Object key) {
		consumer.unsubscribe(key.toString());
	}
	
}
