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
		System.out.println("DEBUG StreamPropagationRoot sendOutputFromModules.1 message=" + streamMessage);
		if (outputsByName == null) {
			initCacheMap();
		}

		System.out.println("DEBUG StreamPropagationRoot sendOutputFromModules.2");
		Map msg = streamMessage.getParsedContent();

		for (Map.Entry<String, List<Output>> entry : outputsByName.entrySet()) {
			System.out.println("DEBUG StreamPropagationRoot sendOutputFromModules.3");
			String fieldName = entry.getKey();
			List<Output> outputs = entry.getValue();

			if (msg.containsKey(fieldName)) {
				System.out.println("DEBUG StreamPropagationRoot sendOutputFromModules.4");
				Object fieldValue = msg.get(fieldName);

				// Null values are just not sent
				if (fieldValue == null) {
					continue;
				}

				System.out.println("DEBUG StreamPropagationRoot sendOutputFromModules.5");
				for (Output o : outputs) {
					// Convert all numbers to doubles
					System.out.println("DEBUG StreamPropagationRoot sendOutputFromModules.6");
					if (o instanceof TimeSeriesOutput) {
						try {
							System.out.println("DEBUG StreamPropagationRoot sendOutputFromModules.7");
							o.send(((Number) fieldValue).doubleValue());
						} catch (ClassCastException e) {
							System.out.println("DEBUG StreamPropagationRoot sendOutputFromModules.8");
							final String s = String.format("Stream field configuration has changed: cannot convert value '%s' to number", fieldValue);
							throw new StreamFieldChangedException(s);
						}
					} else {
						System.out.println("DEBUG StreamPropagationRoot sendOutputFromModules.9");
						o.send(fieldValue);
					}
				}
			}
		}
		System.out.println("DEBUG StreamPropagationRoot sendOutputFromModules.10");
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
