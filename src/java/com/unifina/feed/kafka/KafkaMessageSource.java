package com.unifina.feed.kafka;

import java.util.Map;
import java.util.Properties;

import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractMessageSource;
import com.unifina.feed.Message;
import com.unifina.feed.MessageRecipient;
import com.unifina.feed.MessageSource;
import com.unifina.kafkaclient.UnifinaKafkaConsumer;
import com.unifina.kafkaclient.UnifinaKafkaMessage;
import com.unifina.kafkaclient.UnifinaKafkaMessageHandler;
import com.unifina.utils.MapTraversal;

public class KafkaMessageSource extends AbstractMessageSource<UnifinaKafkaMessage, String> {

	private MessageRecipient<UnifinaKafkaMessage, String> recipient;

	private UnifinaKafkaConsumer consumer;
	
	private UnifinaKafkaMessageHandler handler = new UnifinaKafkaMessageHandler() {
		
		private long counter = 0;
		
		@Override
		public void handleMessage(UnifinaKafkaMessage kafkaMessage, String topic, int partition, long offset) {
			forward(kafkaMessage, topic, counter++, false);
		}
	};
	
	public KafkaMessageSource(Feed feed, Map<String,Object> config) {
		super(feed, config);
		Map<String,Object> kafkaConfig = MapTraversal.flatten((Map) MapTraversal.getMap(config, "unifina.kafka"));
		Properties properties = new Properties();
		for (String s : kafkaConfig.keySet())
			properties.setProperty(s, kafkaConfig.get(s).toString());
		
		consumer = new UnifinaKafkaConsumer(properties);
	}

	@Override
	public void close() {
		consumer.close();
	}

	@Override
	public void subscribe(String key) {
		consumer.subscribe(key.toString(), handler);
	}

	@Override
	public void unsubscribe(String key) {
		consumer.unsubscribe(key.toString());
	}
	
}
