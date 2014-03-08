package com.unifina.feed.kafka;

import java.util.Map;

import com.unifina.data.FeedEvent;
import com.unifina.feed.AbstractEventRecipient;
import com.unifina.signalpath.Output;
import com.unifina.signalpath.kafka.KafkaModule;
import com.unifina.utils.Globals;

public class KafkaMessageRecipient extends AbstractEventRecipient<KafkaModule> {

	public KafkaMessageRecipient(Globals globals) {
		super(globals);
	}

	@Override
	protected void sendOutputFromModules(FeedEvent event) {
		Map msg = ((KafkaMessage) event.content).content;
		
		for (KafkaModule m : modules) {
			// TODO: improve efficiency
			for (Output o : m.getOutputs()) {
				if (msg.containsKey(o.getName()))
					o.send(msg.get(o.getName()));
			}
		}
	}

}
