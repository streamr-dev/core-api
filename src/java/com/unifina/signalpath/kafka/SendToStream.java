package com.unifina.signalpath.kafka;

import com.streamr.client.protocol.message_layer.StreamMessage;
import com.unifina.domain.data.Stream;
import com.unifina.domain.security.SecUser;
import com.unifina.service.PermissionService;
import com.unifina.service.StreamService;
import com.unifina.signalpath.*;
import com.unifina.signalpath.utils.MessageChainUtil;
import grails.converters.JSON;
import grails.util.Holders;
import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONObject;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This module (only) supports sending messages to Kafka/json streams (feed id 7)
 *
 * // TODO: partitioning key
 */
public class SendToStream extends ModuleWithSideEffects {

	private StreamParameter streamParameter = new StreamParameter(this, "stream");

	transient protected PermissionService permissionService = null;
	transient protected StreamService streamService = null;

	private String lastStreamId = null;
	private boolean sendOnlyNewValues = false;
	private List<Input> fieldInputs = new ArrayList<>();

	private MessageChainUtil msgChainUtil;

	@Override
	public void init() {
		// Pre-fetch services for more predictable performance
		ensureServices();

		addInput(streamParameter);
		streamParameter.setUpdateOnChange(true);
		msgChainUtil = new MessageChainUtil(getGlobals().getUserId());
	}

	private void ensureServices() {
		// will be null after deserialization
		if (streamService == null) {
			streamService = Holders.getApplicationContext().getBean(StreamService.class);
		}
		if (permissionService == null) {
			permissionService = Holders.getApplicationContext().getBean(PermissionService.class);
		}
	}

	@Override
	protected boolean allowSideEffectsInHistoricalMode() {
		// SendToStream cannot be configured to really write to the stream in historical mode (for now)
		return false;
	}

	@Override
	public void activateWithSideEffects(){
		ensureServices();
		Stream stream = streamParameter.getValue();
		authenticateStream(stream);
		StreamMessage msg = msgChainUtil.getStreamMessage(stream, getGlobals().time, inputValuesToMap());
		streamService.sendMessage(msg);
	}

	@Override
	protected String getNotificationAboutActivatingWithoutSideEffects() {
		return this.getName()+": In historical mode, events are not written to stream '" + streamParameter.getValue().getName()+"'.";
	}

	private Map<String, Object> inputValuesToMap() {
		Map msg = new LinkedHashMap<>();
		Iterable<Input> inputs = sendOnlyNewValues ? getDrivingInputs() : fieldInputs;
		for (Input i : inputs) {
			msg.put(i.getName(), i.getValue());
		}
		return msg;
	}

	@Override
	public void clearState() {
		msgChainUtil = new MessageChainUtil(getGlobals().getUserId());
	}

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

		if (stream.getConfig()==null) {
			throw new IllegalStateException(this.getName()+": Stream " + stream.getName() +
				" is not properly configured!");
		}

		final JSONObject streamConfig = (JSONObject) JSON.parse(stream.getConfig());
		final JSONArray fields = streamConfig.getJSONArray("fields");

		for (Object o : fields) {
			Input input = null;
			JSONObject j = (JSONObject) o;
			String type = j.getString("type");
			String name = j.getString("name");

			// TODO: add other types
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

		if (streamConfig.containsKey("name"))
			this.setName(streamConfig.get("name").toString());
	}

	private void authenticateStream(Stream stream) {
		// Only check write access in run context to avoid exception when eg. loading and reconstructing canvas
		if (getGlobals().isRunContext() && !stream.getId().equals(lastStreamId) ) {
			if (permissionService == null) {
				permissionService = Holders.getApplicationContext().getBean(PermissionService.class);
			}

			SecUser user = SecUser.getViaJava(getGlobals().getUserId());
			if (permissionService.canWrite(user, stream)) {
				lastStreamId = stream.getId();
			} else {
				throw new AccessControlException(this.getName() + ": User " + user.getUsername() +
					" does not have write access to Stream " + stream.getName());
			}
		}
	}

}
