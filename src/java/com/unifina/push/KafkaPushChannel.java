package com.unifina.push;

import grails.converters.JSON;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.unifina.service.KafkaService;

public class KafkaPushChannel extends PushChannel {

	private JSON json = new JSON();
	private KafkaService kafkaService;

	private static final Logger log = Logger.getLogger(KafkaPushChannel.class);
	
	HashMap<String, Object> byeMsg;
	private boolean sendByeOnDestroy;
	
	/**
	 * @param kafkaService
	 * @param sendByeOnDestroy Sends a special 'bye' message when this channel is destroyed. 'Bye' is meant to be final, so only set it to true for adhoc channels.
	 */
	public KafkaPushChannel(KafkaService kafkaService, boolean sendByeOnDestroy) {
		super();
		this.kafkaService = kafkaService;
		this.sendByeOnDestroy = sendByeOnDestroy;
		byeMsg = new HashMap<>();
		byeMsg.put("_bye", true);
	}
	
	@Override
	public void destroy() {
		if (sendByeOnDestroy) {
			for (String channel : channels) {
				log.info("Sending bye message to "+channel);
				push(byeMsg, channel);
			}
		}
		super.destroy();
	}
	
	@Override
	public void addChannel(String channel) {
		super.addChannel(channel);
		
		// Explicitly create the topics
		ArrayList<String> topics = new ArrayList<>(1);
		topics.add(channel);
		log.info("Creating channel "+channel);
		kafkaService.createTopics(topics);
		log.info("Channel "+channel+" created");
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
