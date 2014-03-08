package com.unifina.feed.kafka;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;

import com.unifina.feed.Message;
import com.unifina.feed.MessageRecipient;
import com.unifina.feed.MessageSource;

public class KafkaMessageSource implements MessageSource, Runnable {

	private MessageRecipient recipient;
//	private long expected;
	
	private String topic;
	private KafkaStream<byte[], byte[]> stream;
	private ConsumerConfig config;
	private ConsumerConnector consumer;

	private boolean quit = false;
	
	private long firstOffset = -1;
	
	public KafkaMessageSource(String topic, Properties props) {
		this.topic = topic;
		config = new ConsumerConfig(props);
		consumer = Consumer.createJavaConsumerConnector(config);
		
		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
		topicCountMap.put(topic, 1);
		
		Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
		List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);
		stream = streams.get(0);
	}
	
	@Override
	public void setRecipient(MessageRecipient recipient) {
		this.recipient = recipient;
	}

	@Override
	public void setExpectedCounter(long expected) {
//		this.expected = expected;
	}

	@Override
	public void run() {
        ConsumerIterator<byte[], byte[]> it = stream.iterator();
        while (!quit && it.hasNext()) {
        	MessageAndMetadata<byte[], byte[]> mm = it.next();
        	// TODO: fix this.. force the message counter to start from zero every time, because there is no catchup
        	if (firstOffset==-1L) {
        		firstOffset = mm.offset();
        	}
        	
        	Message msg = new Message(mm.offset()-firstOffset, new RawKafkaMessage(topic, new Date(), mm.message()));
        	msg.checkCounter = false;
        	recipient.receive(msg);
        }
	}

	public void quit() {
		quit = true;
	}
	
}
