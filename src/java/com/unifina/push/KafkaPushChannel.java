package com.unifina.push;

import com.unifina.domain.data.Stream;
import com.unifina.service.KafkaService;
import grails.converters.JSON;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

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
			// UI channels don't exist as streams in the database
			Stream s = new Stream();
			s.setId(msg.getChannel());
			// UI channels always have one partition
			s.setPartitions(1);

			kafkaService.sendMessage(s, /*partition key*/ null, (Map) msg.getContent(), msg.getTTL());
		}
		else throw new IllegalArgumentException("Unsupported content type: "+msg.getContent());
	}
	
	public boolean isConnected() {
		return true;
	}

}
