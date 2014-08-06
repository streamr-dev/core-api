package com.unifina.feed.map;

import java.util.Map;

import com.unifina.data.FeedEvent;
import com.unifina.domain.data.Stream;
import com.unifina.feed.StreamEventRecipient;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Output;
import com.unifina.utils.Globals;

/**
 * This class receives FeedEvents with MapMessage content. It sends out
 * the values in the MapMessage if the receiving module has an output
 * with a corresponding name. Other values are ignored.
 * 
 * Note that the type of value is unchecked and must match with the output type.
 * @author Henri
 */
public class MapMessageEventRecipient extends StreamEventRecipient<AbstractSignalPathModule> {

	public MapMessageEventRecipient(Globals globals, Stream stream) {
		super(globals, stream);
	}

	@Override
	protected void sendOutputFromModules(FeedEvent event) {
		Map msg = ((MapMessage) event.content).content;
		
		for (AbstractSignalPathModule m : modules) {
			// TODO: improve efficiency
			for (Output o : m.getOutputs()) {
				if (msg.containsKey(o.getName()))
					o.send(msg.get(o.getName()));
			}
		}
	}

}
