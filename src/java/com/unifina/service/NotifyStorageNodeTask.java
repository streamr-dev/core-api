package com.unifina.service;

import com.streamr.client.StreamrClient;
import com.unifina.domain.EthereumAddress;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

final class NotifyStorageNodeTask implements Runnable {
	private static final Logger log = Logger.getLogger(NotifyStorageNodeTask.class);

	static final class ErrorHandler implements Thread.UncaughtExceptionHandler {
		@Override
		public void uncaughtException(Thread t, Throwable e) {
			String s = String.format("error while processing storage node request %s", t.getName());
			log.error(s, e);
		}
	}

	public enum AssigmentEvent {
		STREAM_ADDED,
		STREAM_REMOVED
	}

	private final EthereumAddress storageNodeAddress;
	private final String streamId;
	private final AssigmentEvent eventType;
	private final StreamService streamService;
	private final StreamrClientService streamrClientService;

	public NotifyStorageNodeTask(
			final EthereumAddress storageNodeAddress,
			final String streamId,
			final AssigmentEvent eventType,
			final StreamService streamService,
			final StreamrClientService streamrClientService) {
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
			String msg = String.format("Unable to notify StorageNode: streamId=%s, event=%s, address=%s", streamId, eventType, storageNodeAddress);
			log.error(msg, e);
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
