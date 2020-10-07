package com.unifina.signalpath.streams;

import com.unifina.domain.Stream;
import com.unifina.domain.User;
import com.unifina.service.CreateStreamCommand;
import com.unifina.service.StreamService;
import com.unifina.service.ValidationException;
import com.unifina.signalpath.*;
import com.unifina.utils.JSONUtil;
import grails.util.Holders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateStream extends AbstractSignalPathModule {

	private final MapParameter fields = new MapParameter(this, "fields");
	private final StringInput nameInput = new StringInput(this, "name");
	private final StringInput description = new StringInput(this, "description");

	private final StringOutput stream = new StringOutput(this, "stream");
	private final BooleanOutput created = new BooleanOutput(this, "created");

	protected final HashMap<String, String> cachedStreamIdsByName = new HashMap<>();
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
			streamService = Holders.getApplicationContext().getBean(StreamService.class);
		}

		if (cachedStreamIdsByName.containsKey(nameInput.getValue())) {
			sendOutputs(false, null);
			return;
		}

		try {
			Stream s = streamService.createStream(buildParams(), User.loadViaJava(getGlobals().getUserId()));
			sendOutputs(true, s.getId());
			cachedStreamIdsByName.put(nameInput.getValue(), s.getId());
		} catch (ValidationException ex) {
			sendOutputs(false, null);
		}
	}

	@Override
	public void clearState() {
		cachedStreamIdsByName.clear();
	}

	private CreateStreamCommand buildParams() {
		CreateStreamCommand cmd = new CreateStreamCommand();
		cmd.setName(nameInput.getValue());
		cmd.setDescription(description.getValue());
		Map<String, Object> config = new HashMap<>();
		config.put("fields", mapToListOfFieldConfigs(fields.getValue()));
		cmd.setConfig(JSONUtil.createGsonBuilder().toJson(config));
		return cmd;
	}

	protected String getStreamName() {
		return nameInput.getValue();
	}

	protected void sendOutputs(boolean createdValue, String streamId) {
		created.send(createdValue);
		if (streamId != null) {
			stream.send(streamId);
		}
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
