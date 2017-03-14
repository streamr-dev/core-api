package com.unifina.push;

import com.unifina.domain.data.Stream;
import com.unifina.service.StreamService;
import grails.util.Holders;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class KafkaPushChannel extends PushChannel {

	private final StreamService streamService;

	private static final Logger log = Logger.getLogger(KafkaPushChannel.class);
	
	HashMap<String, Object> byeMsg;
	private boolean adhoc;

	private static final int ADHOC_TTL_SECONDS = 30*60;
	
	/**
	 * @param adhoc If true, sends a special 'bye' message when this channel is destroyed. 'Bye' is meant to be final, so only set it to true for adhoc channels. Setting adhoc to true also enforces finite TTL on messages.
	 */
	public KafkaPushChannel(boolean adhoc) {
		super();
		this.streamService = Holders.getApplicationContext().getBean(StreamService.class);
		this.adhoc = adhoc;
		byeMsg = new HashMap<>();
		byeMsg.put("_bye", true);
	}
	
	@Override
	public void destroy() {
		if (adhoc) {
			for (String channel : channels) {
				log.info("Sending bye message to "+channel);
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

			streamService.sendMessage(s, (Map) msg.getContent(), (adhoc ? ADHOC_TTL_SECONDS : msg.getTTL()));
		}
		else throw new IllegalArgumentException("Unsupported content type: "+msg.getContent());
	}
	
	public boolean isConnected() {
		return true;
	}

}
