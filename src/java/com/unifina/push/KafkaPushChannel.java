package com.unifina.push;

import grails.converters.JSON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
				push(byeMsg, channel);
			}
		}
		super.destroy();
	}
	
	@Override
	protected void doPush(PushChannelMessage msg) {
		if (msg.getContent() instanceof Map) {
			kafkaService.sendMessage(msg.getChannel(), (Map) msg.getContent());
		}
		else throw new IllegalArgumentException("Unsupported content type: "+msg.getContent());
	}
	
	public boolean isConnected() {
		return true;
	}

}
