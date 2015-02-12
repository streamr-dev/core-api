package com.unifina.push;

import grails.converters.JSON;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.unifina.service.KafkaService;

public class KafkaPushChannel extends PushChannel {

	private JSON json = new JSON();
	private KafkaService kafkaService;

	private static final Logger log = Logger.getLogger(KafkaPushChannel.class);
	
	public KafkaPushChannel(KafkaService kafkaService) {
		super();
		this.kafkaService = kafkaService;
	}
	
	@Override
	public void addChannel(String channel) {
		super.addChannel(channel);
		
		// Explicitly create the topics
		ArrayList<String> topics = new ArrayList<>(1);
		topics.add(channel);
		kafkaService.createTopics(topics);
	}
	
	@Override
	protected void doPush(PushChannelMessage msg) {
		String str = msg.toJSON(json);
		kafkaService.getProducer().sendRaw(msg.getChannel(), "ui", str.getBytes());
	}
	
	public boolean isConnected() {
		return true;
	}

}
