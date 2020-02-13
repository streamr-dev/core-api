package com.unifina.signalpath.kafka;

import com.streamr.client.StreamrClient;
import com.unifina.domain.data.Stream;
import com.unifina.domain.security.Permission;
import com.unifina.domain.security.SecUser;
import com.unifina.service.PermissionService;
import com.unifina.signalpath.*;
import grails.converters.JSON;
import grails.util.Holders;
import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONObject;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This module sends messages to streams.
 */
public class SendToStream extends ModuleWithSideEffects {

	private StreamParameter streamParameter = new StreamParameter(this, "stream");

	transient private StreamrClient streamrClient = null;
	transient private com.streamr.client.rest.Stream cachedStream = null;

	private boolean sendOnlyNewValues = false;

	private List<Input> fieldInputs = new ArrayList<>();
	private Input<Object> partitionKey = null; // Only set if the Stream has multiple partitions

	private static final Logger log = Logger.getLogger(SendToStream.class);

	@Override
	public void init() {
		addInput(streamParameter);
		streamParameter.setUpdateOnChange(true);
	}

	private StreamrClient getStreamrClient() {
		if (streamrClient == null) {
			streamrClient = getGlobals().getStreamrClient();
		}
		return streamrClient;
	}

	@Override
	protected boolean allowSideEffectsInHistoricalMode() {
		// SendToStream cannot be configured to really write to the stream in historical mode (for now)
		return false;
	}

	@Override
	public void activateWithSideEffects(){
		Stream stream = streamParameter.getValue();
		try {
			com.streamr.client.rest.Stream s = cacheStream(stream);
			streamrClient.publish(
				s,
				inputValuesToMap(),
				getGlobals().getTime(),
				partitionKey != null && partitionKey.getValue() != null ? partitionKey.getValue().toString() : null
			);
		} catch (Exception e) {
			log.error("Failed to publish: ", e);
		}

	}

	@Override
	protected String getNotificationAboutActivatingWithoutSideEffects() {
		return this.getName()+": In historical mode, events are not written to stream '" + streamParameter.getValue().getName()+"'.";
	}

	private Map<String, Object> inputValuesToMap() {
		Map msg = new LinkedHashMap<>();
		Iterable<Input> inputs = sendOnlyNewValues ? getDrivingInputs() : fieldInputs;
		for (Input i : inputs) {
			if (i == partitionKey) {
				continue;
			} else {
				msg.put(i.getName(), i.getValue());
			}
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

		// Fail when configuring if the stream is not writable
		if (getGlobals().isRunContext()) {
			checkWriteAccess(stream);
		}

		if (stream.getConfig()==null) {
			throw new IllegalStateException(this.getName()+": Stream " + stream.getName() +
				" is not properly configured!");
		}

		// Add the partitionKey input if the stream has multiple partitions
		if (stream.getPartitions() > 1) {
			partitionKey = new Input<>(this, "partitionKey", "Object");
			partitionKey.setCanToggleDrivingInput(false);
			partitionKey.setRequiresConnection(false);
			addInput(partitionKey);
		}

		final JSONObject streamConfig = (JSONObject) JSON.parse(stream.getConfig());
		final JSONArray fields = streamConfig.getJSONArray("fields");

		for (Object o : fields) {
			Input input = null;
			JSONObject j = (JSONObject) o;
			String type = j.getString("type");
			String name = j.getString("name");

			if (type.equalsIgnoreCase("number")) {
				input = new TimeSeriesInput(this, name);
				((TimeSeriesInput) input).setCanHaveInitialValue(false);
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
				input.setCanToggleDrivingInput(false);
				input.setRequiresConnection(false);
				addInput(input);
				fieldInputs.add(input);
			}
		}

		if (streamConfig.containsKey("name")) {
			this.setName(streamConfig.get("name").toString());
		}
	}

	private com.streamr.client.rest.Stream cacheStream(Stream stream) throws IOException {
		if (cachedStream == null || !stream.getId().equals(cachedStream.getId()) ) {
			cachedStream = getStreamrClient().getStream(stream.getId());
		}
		return cachedStream;
	}

	private void checkWriteAccess(Stream stream) {
		SecUser user = SecUser.getViaJava(getGlobals().getUserId());
		if (!Holders.getApplicationContext().getBean(PermissionService.class).check(user, stream, Permission.Operation.STREAM_PUBLISH)) {
			throw new AccessControlException(this.getName() + ": User " + user.getUsername() +
				" does not have write access to Stream " + stream.getName());
		}
	}

}
