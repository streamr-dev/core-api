package com.unifina.service;

import com.streamr.client.StreamrClient;
import com.unifina.domain.EthereumAddress;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

final class NotifyStorageNodeTask extends Thread {

	private static final Logger log = Logger.getLogger(NotifyStorageNodeTask.class);

	public enum AssigmentEvent {
		STREAM_ADDED,
		STREAM_REMOVED
	}

	final EthereumAddress storageNodeAddress;
	final String streamId;
	final AssigmentEvent eventType;
	final StreamService streamService;
	final StreamrClientService streamrClientService;

	public NotifyStorageNodeTask(EthereumAddress storageNodeAddress, String streamId, AssigmentEvent eventType, StreamService streamService, StreamrClientService streamrClientService) {
		super("NotifyStorageNodeTask-" + System.currentTimeMillis());
		this.storageNodeAddress = storageNodeAddress;
		this.streamId = streamId;
		this.eventType = eventType;
		this.streamService = streamService;
		this.streamrClientService = streamrClientService;
	}

	@Override
	public void run() {
		StreamrClient client = streamrClientService.getInstanceForThisEngineNode();
		try {
			com.streamr.client.rest.Stream assignmentStream = client.getStream(StorageNodeService.createAssignmentStreamId());
			client.publish(assignmentStream, createMessage());
		} catch (Exception e) {
			log.warn("Unable to notify StorageNode: streamId=" + streamId + ", event=" + eventType);
		}
	}

	private Map<String, Object> createMessage() {
		Map<String, Object> message = new HashMap<>();
		message.put("event", eventType.name());
		Map<String, Object> stream = new HashMap<>();
		stream.put("id", streamId);
		stream.put("partitions", streamService.getStream(streamId).getPartitions());
		message.put("stream", stream);
		return message;
	}
}
