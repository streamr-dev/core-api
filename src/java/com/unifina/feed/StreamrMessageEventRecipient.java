package com.unifina.feed;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;
import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractStreamrMessage;
import com.unifina.feed.StreamEventRecipient;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Output;
import com.unifina.signalpath.TimeSeriesOutput;
import com.unifina.utils.Globals;

import java.util.*;

/**
 * This class receives FeedEvents with MapMessage content. It sends out
 * the values in the MapMessage if the receiving module has an output
 * with a corresponding name. Other values are ignored.
 * 
 * Note that the type of value is unchecked and must match with the output type.
 */
public class StreamrMessageEventRecipient extends StreamEventRecipient<AbstractSignalPathModule, AbstractStreamrMessage> {

	Map<String, List<Output>> outputsByName = null;
	
	public StreamrMessageEventRecipient(Globals globals, Stream stream, Set<Integer> partitions) {
		super(globals, stream, partitions);
	}

	private void initCacheMap() {
		outputsByName = new LinkedHashMap<>();
		
		for (AbstractSignalPathModule m : modules) {
			for (Output o : m.getOutputs()) {
				if (!outputsByName.containsKey(o.getName())) {
					outputsByName.put(o.getName(), new ArrayList<Output>());
				}
				
				outputsByName.get(o.getName()).add(o);
			}
		}
	}
	
	@Override
	protected void sendOutputFromModules(FeedEvent<AbstractStreamrMessage, ? extends IEventRecipient> event) {
		if (outputsByName==null)
			initCacheMap();
		
		AbstractStreamrMessage msg = event.content;
		
		for (String name : outputsByName.keySet()) {
			Object val = msg.get(name);

			// Null values are just not sent
			if (val==null) {
				continue;
			}

			for (Output o : outputsByName.get(name)) {
				// Convert all numbers to doubles
				if (o instanceof TimeSeriesOutput) {
					o.send(((Number) val).doubleValue());
				}
				else o.send(val);
			}
		}
	}

}
