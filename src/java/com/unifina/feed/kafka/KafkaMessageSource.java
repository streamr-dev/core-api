package com.unifina.feed.kafka;

import java.util.Properties;

import com.unifina.feed.Message;
import com.unifina.feed.MessageRecipient;
import com.unifina.feed.MessageSource;
import com.unifina.kafkaclient.UnifinaKafkaConsumer;
import com.unifina.kafkaclient.UnifinaKafkaMessage;
import com.unifina.kafkaclient.UnifinaKafkaMessageHandler;

public class KafkaMessageSource implements MessageSource {

	private MessageRecipient recipient;

	private UnifinaKafkaConsumer consumer;
	
	public KafkaMessageSource(String topic, Properties props) {
		consumer = new UnifinaKafkaConsumer(props);
		UnifinaKafkaMessageHandler handler = new UnifinaKafkaMessageHandler() {
			
			private long offset = 0;
			
			@Override
			public void handleMessage(UnifinaKafkaMessage kafkaMessage) {
				Message msg = new Message(offset++, kafkaMessage);
				msg.checkCounter = false;
				recipient.receive(msg);
			}
		};
		
		consumer.subscribe(topic, handler, false);
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
	
}
