package com.unifina.signalpath.utils;

import com.streamr.client.utils.StreamPartition;
import com.unifina.domain.data.Stream;
import com.unifina.signalpath.*;
import com.unifina.utils.MapTraversal;
import grails.converters.JSON;
import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This module creates inputs and outputs on configuration time
 * with regard to a field config for a Stream.
 *
 * The fields config must be a JSON message with a key "fields": a list of
 * (name,type) pairs.
 *
 * The sreamConfig can also define a key "name", which is used as the module name.
 *
 * Output from this module is written via StreamPropagationRoot.
 */
public class ConfigurableStreamModule extends AbstractSignalPathModule {

	private final StreamParameter streamParameter = new StreamParameter(this, "stream");
	private Collection<Integer> selectedPartitions = null;

	private static final Logger log = Logger.getLogger(ConfigurableStreamModule.class);

	@Override
	public void init() {
		streamParameter.setUpdateOnChange(true);
		streamParameter.setDrivingInput(false);
		streamParameter.setCanToggleDrivingInput(false);
		addInput(streamParameter);
	}

	public Stream getStream() {
		return streamParameter.getValue();
	}

	public Collection<StreamPartition> getStreamPartitions() {
		Collection<Integer> selectedPartitionNumbers;

		if (selectedPartitions.isEmpty()) {
			log.warn(String.format("getStreamPartitions: No partitions selected for stream %s, defaulting to all %d partitions",
				streamParameter.getValue().getId(), streamParameter.getValue().getPartitions()));
			selectedPartitionNumbers = allPartitions();
		} else {
			selectedPartitionNumbers = selectedPartitions;
		}

		return selectedPartitionNumbers.stream()
			.map((partition) -> new StreamPartition(getStream().getId(), partition))
			.collect(Collectors.toList());
	}

	private Collection<Integer> allPartitions() {
		ArrayList<Integer> result = new ArrayList<>(getStream().getPartitions());
		for (int i=0; i<getStream().getPartitions(); i++) {
			result.add(i);
		}
		return result;
	}

	@Override
	public void sendOutput() {
		// StreamPropagationRoot sends values to the outputs of this module
	}

	@Override
	public void clearState() {}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		Stream stream = getStream();
		if (stream == null) {
			return;
		}
		if (stream.getConfig() == null) {
			String msg = String.format("Stream %s is not properly configured!", stream.getName());
			throw new IllegalStateException(msg);
		}

		JSONObject streamConfig = (JSONObject)JSON.parse(stream.getConfig());
		JSONArray fields = streamConfig.getJSONArray("fields");

		for (Object o : fields) {
			JSONObject j = (JSONObject)o;
			String type = j.getString("type");
			String name = j.getString("name");

			Output output = null;

			if (type.equalsIgnoreCase("number")) {
				output = new TimeSeriesOutput(this, name);
			} else if (type.equalsIgnoreCase("string")) {
				output = new StringOutput(this, name);
			} else if (type.equalsIgnoreCase("boolean")) {
				output = new BooleanOutput(this, name);
			} else if (type.equalsIgnoreCase("map")) {
				output = new MapOutput(this, name);
			} else if (type.equalsIgnoreCase("list")) {
				output = new ListOutput(this, name);
			}

			if (output != null) {
				if (output instanceof PrimitiveOutput) {
					((PrimitiveOutput) output).setNoRepeat(false);
				}
				addOutput(output);
			}
		}

		if (streamConfig.containsKey("name")) {
			this.setName(streamConfig.get("name").toString());
		}

		if (config.containsKey("partitions")) {
			selectedPartitions = MapTraversal.getList(config, "partitions");
			log.debug(String.format("onConfiguration: read selected partitions from config: stream %s (%d partitions): %s", stream.getId(), stream.getPartitions(), selectedPartitions));
		} else {
			// Default to all partitions selected
			selectedPartitions = allPartitions();
			log.debug(String.format("onConfiguration: no partition config found, defaulting to all: stream %s (%d partitions): %s", stream.getId(), stream.getPartitions(), selectedPartitions));
		}
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		if (streamParameter.getValue() != null) {
			if (selectedPartitions == null) {
				selectedPartitions = allPartitions();
			}
			config.put("partitions", selectedPartitions);
		}

		return config;
	}
}
