package com.unifina.signalpath.utils;

import com.streamr.client.utils.StreamPartition;
import com.unifina.domain.data.Stream;
import com.unifina.signalpath.*;
import grails.converters.JSON;
import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
	transient protected JSONObject streamConfig = null;

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

	/**
	 * TODO: add mechanism for selecting which partitions are needed.
	 * Current implementation wants all partitions.
	 */

	public Collection<Integer> getPartitions() {
		List<Integer> arr = new ArrayList<>(getStream().getPartitions());
		for (int i=0; i<getStream().getPartitions(); i++) {
			arr.add(i);
		}
		return arr;
	}

	public Collection<StreamPartition> getStreamPartitions() {
		List<StreamPartition> arr = new ArrayList<>(getStream().getPartitions());
		for (int i=0; i<getStream().getPartitions(); i++) {
			arr.add(new StreamPartition(getStream().getId(), i));
		}
		return arr;
	}

	@Override
	public void sendOutput() {
		// StreamPropagationRoot sends values to the outputs of this module
	}

	@Override
	public void clearState() {

	}

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
		streamConfig = (JSONObject)JSON.parse(stream.getConfig());

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

		if (streamConfig.containsKey("name"))
			this.setName(streamConfig.get("name").toString());
	}

}
