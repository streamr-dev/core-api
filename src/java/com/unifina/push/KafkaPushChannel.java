package com.unifina.push;

import grails.converters.JSON;

import java.util.Map;

import org.apache.log4j.Logger;

import com.unifina.kafkaclient.UnifinaKafkaUtils;
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
	public void destroy() {
		super.destroy();
		
		// Mark the topics for deletion
		UnifinaKafkaUtils utils = kafkaService.createUtils();
		for (String topic : getChannels()) {
			utils.deleteTopic(topic);
		}
		utils.close();
	}
	
	@Override
	protected void doPush(PushChannelMessage msg) {
		((Map) msg.getContent()).put("channel", msg.getChannel());
		String str = msg.toJSON(json);
		kafkaService.getProducer().sendRaw(msg.getChannel(), "ui", str.getBytes());
	}
	
	public boolean isConnected() {
		return true;
	}

}
