package com.unifina.signalpath.kafka;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;
import com.unifina.domain.data.Feed;
import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractFeed;
import com.unifina.feed.kafka.KafkaMessage;
import com.unifina.service.KafkaService;
import com.unifina.service.PermissionService;
import com.unifina.signalpath.*;
import com.unifina.utils.Globals;
import grails.converters.JSON;
import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONObject;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This module (only) supports sending messages to Kafka/json streams (feed id 7)
 */
public class SendToStream extends ModuleWithSideEffects {

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
	protected boolean allowSideEffectsInHistoricalMode() {
		// SendToStream cannot be configured to really write to the stream in historical mode (for now)
		return false;
	}

	@Override
	public void activateWithSideEffects() {
		if (kafkaService == null) { // null after de-serialization
			kafkaService = getGlobals().getBean(KafkaService.class);
		}
		Stream stream = streamParameter.getValue();
		authenticateStream(stream);
		kafkaService.sendMessage(stream, "", inputValuesToMap());
	}

	@Override
	protected void activateWithoutSideEffects() {
		Globals globals = getGlobals();

		// Create the message locally and route it to the stream locally, without actually producing to the stream
		KafkaMessage kafkaMessage = new KafkaMessage(streamParameter.getValue().getId(), globals.time, globals.time, inputValuesToMap());

		// Find the Feed implementation for the target Stream
		AbstractFeed feed = getGlobals().getDataSource().getFeedById(streamParameter.getValue().getFeed().getId());

		// Find the IEventRecipient for this message
		IEventRecipient eventRecipient = feed.getEventRecipientForMessage(kafkaMessage);

		FeedEvent event = new FeedEvent(kafkaMessage, globals.time, eventRecipient);
		getGlobals().getDataSource().getEventQueue().enqueue(event);
	}

	@Override
	protected String getNotificationAboutActivatingWithoutSideEffects() {
		return this.getName()+": In historical mode, events written to Stream '" + streamParameter.getValue().getName()+"' are only available within this Canvas.";
	}

	private Map<String, Object> inputValuesToMap() {
		Map msg = new LinkedHashMap<>();
		Iterable<Input> inputs = sendOnlyNewValues ? drivingInputs : fieldInputs;
		for (Input i : inputs) {
			msg.put(i.getName(), i.getValue());
		}
		return msg;
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
