package com.unifina.signalpath.kafka;

import com.streamr.client.protocol.message_layer.MessageID;
import com.streamr.client.protocol.message_layer.MessageRef;
import com.streamr.client.protocol.message_layer.StreamMessage;
import com.streamr.client.protocol.message_layer.StreamMessageV30;
import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;
import com.unifina.data.StreamPartitioner;
import com.unifina.domain.data.Feed;
import com.unifina.domain.data.Stream;
import com.unifina.domain.security.SecUser;
import com.unifina.feed.AbstractFeed;
import com.unifina.service.PermissionService;
import com.unifina.service.StreamService;
import com.unifina.signalpath.*;
import com.unifina.utils.Globals;
import grails.converters.JSON;
import grails.util.Holders;
import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONObject;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.*;

/**
 * This module (only) supports sending messages to Kafka/json streams (feed id 7)
 *
 * // TODO: partitioning key
 */
public class SendToStream extends ModuleWithSideEffects {

	protected StreamParameter streamParameter = new StreamParameter(this, "stream");
	transient protected JSONObject streamConfig = null;

	transient protected PermissionService permissionService = null;
	transient protected StreamService streamService = null;

	protected boolean historicalWarningShown = false;
	private String lastStreamId = null;
	private boolean sendOnlyNewValues = false;
	private List<Input> fieldInputs = new ArrayList<>();

	private Map<String,Long> previousTimestamps = new HashMap<>();
	private Map<String,Long> previousSequenceNumbers = new HashMap<>();

	private static final Logger log = Logger.getLogger(SendToStream.class);

	private long getNextSequenceNumber(String streamId, long timestamp) {
		if (!previousTimestamps.containsKey(streamId) || previousTimestamps.get(streamId) != timestamp) {
			return 0L;
		}
		if (!previousSequenceNumbers.containsKey(streamId)) {
			previousSequenceNumbers.put(streamId, 0L);
		}
		return previousSequenceNumbers.get(streamId)+1;
	}

	private MessageRef getPreviousMessageRef(String streamId) {
		if (!previousTimestamps.containsKey(streamId)) {
			return null;
		}
		if (!previousSequenceNumbers.containsKey(streamId)) {
			previousSequenceNumbers.put(streamId, 0L);
		}
		return new MessageRef(previousTimestamps.get(streamId), previousSequenceNumbers.get(streamId));
	}

	@Override
	public void init() {
		// Pre-fetch services for more predictable performance
		ensureServices();

		addInput(streamParameter);
		streamParameter.setUpdateOnChange(true);
		previousTimestamps = new HashMap<>();
		previousSequenceNumbers = new HashMap<>();

		// TODO: don't rely on static ids
		Feed feedFilter = new Feed();
		feedFilter.setId(7L);
		streamParameter.setFeedFilter(feedFilter);
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

	private StreamMessage getMsg(Stream stream){
		int streamPartition = StreamPartitioner.partition(stream, null);
		long timestamp = System.currentTimeMillis();
		long sequenceNumber = getNextSequenceNumber(stream.getId(), timestamp);
		SecUser user = SecUser.getViaJava(getGlobals().getUserId());
		String publisherId = user == null ? "" : user.getPublisherId();
		MessageID msgId = new MessageID(stream.getId(), streamPartition, timestamp, sequenceNumber, publisherId);
		MessageRef prevMsgRef = this.getPreviousMessageRef(stream.getId());
		try {
			StreamMessage msg = new StreamMessageV30(msgId, prevMsgRef, StreamMessage.ContentType.CONTENT_TYPE_JSON,
					inputValuesToMap(), StreamMessage.SignatureType.SIGNATURE_TYPE_NONE, null);
			previousTimestamps.put(stream.getId(), timestamp);
			previousSequenceNumbers.put(stream.getId(), sequenceNumber);
			return msg;
		} catch (IOException e) {
			log.error(e);
		}
		return null;
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
		StreamMessage msg = getMsg(stream);
		streamService.sendMessage(msg);
	}

	@Override
	protected void activateWithoutSideEffects(){
		Globals globals = getGlobals();

		// Create the message locally and route it to the stream locally, without actually producing to the stream
		StreamMessage msg = getMsg(streamParameter.getValue());

		// Find the Feed implementation for the target Stream
		AbstractFeed feed = getGlobals().getDataSource().getFeedById(streamParameter.getValue().getFeed().getId());

		// Find the IEventRecipient for this message
		IEventRecipient eventRecipient = feed.getEventRecipientForMessage(msg);

		if (eventRecipient != null) {
			FeedEvent event = new FeedEvent(msg, globals.time, eventRecipient);
			getGlobals().getDataSource().enqueueEvent(event);
		}
	}

	@Override
	protected String getNotificationAboutActivatingWithoutSideEffects() {
		return this.getName()+": In historical mode, events written to Stream '" + streamParameter.getValue().getName()+"' are only available within this Canvas.";
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
		previousTimestamps = new HashMap<>();
		previousSequenceNumbers = new HashMap<>();
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
