package com.unifina.signalpath.kafka;

import com.unifina.domain.data.Feed;
import com.unifina.domain.data.Stream;
import com.unifina.service.KafkaService;
import com.unifina.service.PermissionService;
import com.unifina.signalpath.*;
import grails.converters.JSON;
import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONObject;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SendToStream extends AbstractSignalPathModule {

	protected StreamParameter streamParameter = new StreamParameter(this, "stream");
	transient protected JSONObject streamConfig = null;

	transient protected PermissionService permissionService = null;
	transient protected KafkaService kafkaService = null;

	protected boolean historicalWarningShown = false;
	private String lastStreamId = null;
	private boolean sendOnlyNewValues = false;
	private List<Input> fieldInputs = new ArrayList<>();

	@Override
	public void init() {
		// Pre-fetch services for more predictable performance
		permissionService = getGlobals().getBean(PermissionService.class);
		kafkaService = getGlobals().getBean(KafkaService.class);

		addInput(streamParameter);


		streamParameter.setUpdateOnChange(true);

		// TODO: don't rely on static ids
		Feed feedFilter = new Feed();
		feedFilter.setId(7L);
		streamParameter.setFeedFilter(feedFilter);
	}

	@Override
	public void sendOutput() {
		if (getGlobals().isRealtime()) {
			Map msg = new LinkedHashMap<>();
			Iterable<Input> inputs = sendOnlyNewValues ? drivingInputs : fieldInputs;
			for (Input i : inputs) {
				msg.put(i.getName(), i.getValue());
			}
			if (kafkaService == null) { // null after de-serialization
				kafkaService = getGlobals().getBean(KafkaService.class);
			}
			Stream stream = streamParameter.getValue();
			authenticateStream(stream);
			kafkaService.sendMessage(stream, "", msg);
		}
		else if (!historicalWarningShown && getGlobals().getUiChannel()!=null) {
			getGlobals().getUiChannel().push(new NotificationMessage(this.getName()+": Not sending to Stream '" +
				streamParameter.getValue()+"' in historical playback mode."), parentSignalPath.getUiChannelId());
			historicalWarningShown = true;
		}
	}

	@Override
	public void clearState() {}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		ModuleOptions options = ModuleOptions.get(config);
		options.addIfMissing(ModuleOption.createBoolean("sendOnlyNewValues", sendOnlyNewValues));
		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		Stream stream = streamParameter.getValue();

		ModuleOptions options = ModuleOptions.get(config);
		ModuleOption sendOnlyNewValuesOption = options.getOption("sendOnlyNewValues");
		if (sendOnlyNewValuesOption != null) {
			sendOnlyNewValues = sendOnlyNewValuesOption.getBoolean();
		}

		if (stream==null)
			return;

		authenticateStream(stream);

		if (stream.getFeed().getId() != Feed.KAFKA_ID) {
			throw new IllegalArgumentException(this.getName()+": Unable to write to stream type: " +
				stream.getFeed().getName());
		}

		if (stream.getConfig()==null) {
			throw new IllegalStateException(this.getName()+": Stream " + stream.getName() +
				" is not properly configured!");
		}

		streamConfig = (JSONObject) JSON.parse(stream.getConfig());

		JSONArray fields = streamConfig.getJSONArray("fields");

		for (Object o : fields) {
			Input input = null;
			JSONObject j = (JSONObject) o;
			String type = j.getString("type");
			String name = j.getString("name");

			// TODO: add other types
			if (type.equalsIgnoreCase("number")) {
				input = new TimeSeriesInput(this, name);
				((TimeSeriesInput) input).canHaveInitialValue = false;
			} else if (type.equalsIgnoreCase("boolean")) {
				input = new BooleanInput(this, name);
			} else if (type.equalsIgnoreCase("string")) {
				input = new StringInput(this, name);
			} else if (type.equalsIgnoreCase("map")) {
				input = new MapInput(this, name);
			} else if (type.equalsIgnoreCase("list")) {
				input = new ListInput(this, name);
			}

			if (input != null) {
				input.canToggleDrivingInput = false;
				input.canBeFeedback = false;
				input.requiresConnection = false;
				addInput(input);
				fieldInputs.add(input);
			}
		}

		if (streamConfig.containsKey("name"))
			this.setName(streamConfig.get("name").toString());
	}

	private void authenticateStream(Stream stream) {
		// Only check write access in run context to avoid exception when eg. loading and reconstructing canvas
		if (getGlobals().isRunContext() && !stream.getId().equals(lastStreamId) ) {
			if (permissionService == null) {
				permissionService = getGlobals().getBean(PermissionService.class);
			}

			if (permissionService.canWrite(getGlobals().getUser(), stream)) {
				lastStreamId = stream.getId();
			} else {
				throw new AccessControlException(this.getName() + ": User " + getGlobals().getUser().getUsername() +
					" does not have write access to Stream " + stream.getName());
			}
		}
	}

}
