package com.unifina.feed.kafka;

import java.util.Map;

import com.unifina.data.FeedEvent;
import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractEventRecipient;
import com.unifina.signalpath.Output;
import com.unifina.signalpath.kafka.KafkaModule;
import com.unifina.utils.Globals;

public class KafkaMessageRecipient extends AbstractEventRecipient<KafkaModule> {

	private Stream stream;
	private String topic;

	public KafkaMessageRecipient(Globals globals, Stream stream, String topic) {
		super(globals);
		this.stream = stream;
		this.topic = topic;
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

	public Stream getStream() {
		return stream;
	}
	
	public String getTopic() {
		return topic;
	}

}
