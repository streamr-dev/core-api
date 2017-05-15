package com.unifina.signalpath.streams;

import com.unifina.api.ValidationException;
import com.unifina.domain.data.Stream;
import com.unifina.service.StreamService;
import com.unifina.signalpath.*;

import java.util.*;

public class CreateStream extends AbstractSignalPathModule {

	private final MapParameter fields = new MapParameter(this, "fields");
	private final StringInput nameInput = new StringInput(this, "name");
	private final StringInput description = new StringInput(this, "description");

	private final StringOutput stream = new StringOutput(this, "stream");
	private final BooleanOutput created = new BooleanOutput(this, "created");

	private final Set<String> createdStreams = new HashSet<>();
	private transient StreamService streamService;

	@Override
	public void init() {
		addInput(fields);
		addInput(nameInput);
		addInput(description);
		addOutput(created);
		addOutput(stream);
	}

	@Override
	public void sendOutput() {
		if (streamService == null) {
			streamService = getGlobals().getBean(StreamService.class);
		}

		if (createdStreams.contains(nameInput.getValue())) {
			sendOutputs(false, null);
			return;
		}

		try {
			Stream s = streamService.createStream(buildParams(), getGlobals().getUser());
			sendOutputs(true, s);
			createdStreams.add(nameInput.getValue());
		} catch (ValidationException ex) {
			sendOutputs(false, null);
		}
	}

	@Override
	public void clearState() {
		createdStreams.clear();
	}

	protected Map<String, Object> buildParams() {
		Map<String, Object> params = new HashMap<>();
		params.put("name", nameInput.getValue());
		params.put("description", description.getValue());
		Map<String, Object> config = new HashMap<>();
		config.put("fields", mapToListOfFieldConfigs(fields.getValue()));
		params.put("config", config);
		return params;
	}

	protected String getStreamName() {
		return nameInput.getValue();
	}

	protected void sendOutputs(boolean createdValue, Stream streamValue) {
		created.send(createdValue);
		if (streamValue != null) stream.send(streamValue.getId());
	}

	private static List<Map<String, String>> mapToListOfFieldConfigs(Map<String, String> map) {
		List<Map<String, String>> maps = new ArrayList<>();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			Map<String, String> fieldConfig = new HashMap<>();
			fieldConfig.put("name", entry.getKey());
			fieldConfig.put("type", entry.getValue());
			maps.add(fieldConfig);
		}
		return maps;
	}
}
