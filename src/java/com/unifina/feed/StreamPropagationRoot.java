package com.unifina.feed;

import com.streamr.client.protocol.message_layer.StreamMessage;
import com.unifina.datasource.DataSource;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Output;
import com.unifina.signalpath.TimeSeriesOutput;
import com.unifina.signalpath.utils.ConfigurableStreamModule;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class receives StreamMessages and maps their payloads to the outputs
 * of a set of registered modules. The payload fields are mapped to outputs
 * by name. Fields without a matching output, and outputs without a matching field
 * are ignored.
 *
 * Note that the value type is unchecked and must match with the output type.
 */
public class StreamPropagationRoot extends AbstractPropagationRoot<ConfigurableStreamModule, StreamMessage> {

	private Map<String, List<Output>> outputsByName = null;

	public StreamPropagationRoot(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	protected void sendOutputFromModules(StreamMessage streamMessage) {
		if (outputsByName == null) {
			initCacheMap();
		}

		Map msg = streamMessage.getParsedContent();

		for (Map.Entry<String, List<Output>> entry : outputsByName.entrySet()) {
			String fieldName = entry.getKey();
			List<Output> outputs = entry.getValue();

			if (msg.containsKey(fieldName)) {
				Object fieldValue = msg.get(fieldName);

				// Null values are just not sent
				if (fieldValue == null) {
					continue;
				}

				for (Output o : outputs) {
					// Convert all numbers to doubles
					if (o instanceof TimeSeriesOutput) {
						try {
							o.send(((Number) fieldValue).doubleValue());
						} catch (ClassCastException e) {
							final String s = String.format("Stream field configuration has changed: cannot convert value '%s' to number", fieldValue);
							throw new StreamFieldChangedException(s);
						}
					} else {
						o.send(fieldValue);
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
					outputsByName.put(o.getName(), new ArrayList<>());
				}

				outputsByName.get(o.getName()).add(o);
			}
		}
	}

}
