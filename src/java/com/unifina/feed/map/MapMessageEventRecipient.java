package com.unifina.feed.map;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;
import com.unifina.domain.data.Stream;
import com.unifina.exceptions.StreamFieldChangedException;
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
 * @author Henri
 */
public class MapMessageEventRecipient extends StreamEventRecipient<AbstractSignalPathModule, MapMessage> {

	private Map<String, List<Output>> outputsByName = null;

	public MapMessageEventRecipient(Globals globals, Stream stream, Set<Integer> partitions) {
		super(globals, stream, partitions);
	}

	@Override
	protected void sendOutputFromModules(FeedEvent<MapMessage, ? extends IEventRecipient> event) {
		if (outputsByName == null) {
			initCacheMap();
		}

		Map msg = event.content.payload;

		for (String name : outputsByName.keySet()) {
			if (msg.containsKey(name)) {
				Object val = msg.get(name);

				// Null values are just not sent
				if (val == null) {
					continue;
				}

				for (Output o : outputsByName.get(name)) {
					// Convert all numbers to doubles
					if (o instanceof TimeSeriesOutput) {
						try {
							o.send(((Number) val).doubleValue());
						} catch (ClassCastException e) {
							final String s = String.format("Stream field configuration has changed: cannot convert value '%s' to number", val);
							throw new StreamFieldChangedException(s);
						}
					} else {
						o.send(val);
					}
				}
			}
		}
	}

	private void initCacheMap() {
		outputsByName = new LinkedHashMap<>();

		for (AbstractSignalPathModule m : getModules()) {
			for (Output o : m.getOutputs()) {
				if (!outputsByName.containsKey(o.getName())) {
					outputsByName.put(o.getName(), new ArrayList<Output>());
				}

				outputsByName.get(o.getName()).add(o);
			}
		}
	}
}
