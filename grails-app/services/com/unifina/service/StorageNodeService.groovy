package com.unifina.service

import com.streamr.client.StreamrClient
import com.unifina.domain.Stream
import com.unifina.domain.StreamStorageNode
import com.unifina.domain.EthereumAddress
import com.unifina.utils.ApplicationConfig
import grails.compiler.GrailsCompileStatic

enum AssigmentEvent {
	STREAM_ADDED,
	STREAM_REMOVED
}

class NotifyStorageNodeTask extends Thread {
	EthereumAddress storageNodeAddress
	String streamId
	AssigmentEvent eventType
	StreamService streamService
	StreamrClientService streamrClientService

	NotifyStorageNodeTask(EthereumAddress storageNodeAddress, String streamId, AssigmentEvent eventType, StreamService streamService, StreamrClientService streamrClientService) {
		super("NotifyStorageNodeTask-" + System.currentTimeMillis())
		this.storageNodeAddress = storageNodeAddress
		this.streamId = streamId
		this.eventType = eventType
		this.streamService = streamService;
		this.streamrClientService = streamrClientService
	}

	void run() {
		Map<String,Object> message = new LinkedHashMap([
			event: eventType.name(),
			stream: [
				id: streamId,
				partitions: streamService.getStream(streamId).partitions
			]
		])
		StreamrClient client = streamrClientService.getInstanceForThisEngineNode()
		com.streamr.client.rest.Stream assignmentStream = client.getStream(StorageNodeService.createAssignmentStreamId())
		client.publish(assignmentStream, message)
	}
}

@GrailsCompileStatic
class StorageNodeService {

	StreamService streamService
	StreamrClientService streamrClientService

	List<Stream> findStreamsByStorageNode(EthereumAddress storageNodeAddress) {
		List<StreamStorageNode> items = StreamStorageNode.findAllByStorageNodeAddress(storageNodeAddress.toString())
		Iterable<Serializable> streamIds = items.collect{ it.streamId } as Iterable<Serializable>
		return Stream.getAll(streamIds)
	}

	List<StreamStorageNode> findStorageNodesByStream(String streamId) {
		boolean streamExists = (Stream.get(streamId) != null)
		if (streamExists) {
			return StreamStorageNode.findAllByStreamId(streamId)
		} else {
			throw new NotFoundException("Stream", streamId)
		}
	}

	StreamStorageNode addStorageNodeToStream(EthereumAddress storageNodeAddress, String streamId) {
		boolean exists = (StreamStorageNode.findByStorageNodeAddressAndStreamId(storageNodeAddress.toString(), streamId) != null)
		if (!exists) {
			StreamStorageNode instance = new StreamStorageNode(
				streamId: streamId,
				storageNodeAddress: storageNodeAddress.toString()
			)
			StreamStorageNode saved = instance.save(validate: true)
			new NotifyStorageNodeTask(storageNodeAddress, streamId, AssigmentEvent.STREAM_ADDED, streamService, streamrClientService).start()
			return saved;
		} else {
			throw new DuplicateNotAllowedException("StorageNode", storageNodeAddress.toString())
		}
	}

	void removeStorageNodeFromStream(EthereumAddress storageNodeAddress, String streamId) {
		StreamStorageNode instance = StreamStorageNode.findByStorageNodeAddressAndStreamId(storageNodeAddress.toString(), streamId)
		if (instance != null) {
			instance.delete()
			new NotifyStorageNodeTask(storageNodeAddress, streamId, AssigmentEvent.STREAM_REMOVED, streamService, streamrClientService).start()
		} else {
			throw new NotFoundException("StorageNode", storageNodeAddress.toString())
		}
	}

	public static String createAssignmentStreamId() {
		EthereumAddress nodeAddress = EthereumAddress.fromPrivateKey(ApplicationConfig.getString("streamr.ethereum.nodePrivateKey"))
		return nodeAddress.toString() + "/storage-node-assignments"
	}
}
