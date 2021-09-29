package com.unifina.service

import com.streamr.client.StreamrClient
import com.unifina.domain.EthereumAddress
import com.unifina.domain.Stream
import com.unifina.domain.StreamStorageNode
import com.unifina.utils.ApplicationConfig
import grails.compiler.GrailsCompileStatic
import org.apache.log4j.Logger

@GrailsCompileStatic
class StorageNodeService {
	private static final Logger log = Logger.getLogger(StorageNodeService.class)
	StreamService streamService
	StreamrClientService streamrClientService

	List<Stream> findStreamsByStorageNode(EthereumAddress storageNodeAddress) {
		return Stream.findAll("FROM Stream WHERE id IN (SELECT streamId FROM StreamStorageNode WHERE storageNodeAddress=?)", [storageNodeAddress.toString()])
	}

	Set<StreamStorageNode> findStorageNodesByStream(String streamId) {
		Stream stream = streamService.getStream(streamId)
		if (stream != null) {
			return stream.getStorageNodes()
		} else {
			throw new NotFoundException("Stream", streamId)
		}
	}

	StreamStorageNode findStorageNodeByAddress(Stream stream, EthereumAddress address) {
		for (final StreamStorageNode node : stream.getStorageNodes()) {
			if (node.getStorageNodeAddress() == address.toString()) {
				return node
			}
		}
		return null
	}

	StreamStorageNode addStorageNodeToStream(EthereumAddress storageNodeAddress, String streamId) {
		Stream stream = streamService.getStream(streamId)
		if (stream == null) {
			throw new NotFoundException("Stream", streamId)
		}
		StreamStorageNode node = findStorageNodeByAddress(stream, storageNodeAddress)
		if (!node) {
			node = new StreamStorageNode(
				storageNodeAddress: storageNodeAddress.toString(),
				streamId: streamId)
			stream.addToStorageNodes(node)
			stream.save(validate: true, failOnError: true)
			notifyStorageNode(storageNodeAddress, streamId, StreamStorageNode.AssigmentEvent.STREAM_ADDED)
		}
		return node
	}

	private void notifyStorageNode(EthereumAddress storageNodeAddress, String streamId, StreamStorageNode.AssigmentEvent assigmentEvent) {
		StreamrClient client = streamrClientService.getInstanceForThisEngineNode();
		try {
			com.streamr.client.rest.Stream assignmentStream = client.getStream(StorageNodeService.createAssignmentStreamId())
			Map<String, Object> message = createMessage(
				storageNodeAddress,
				streamId,
				assigmentEvent)
			client.publish(assignmentStream, message)
		} catch (Exception e) {
			String msg = String.format("Unable to notify StorageNode: streamId=%s, event=%s, address=%s", streamId, assigmentEvent, storageNodeAddress)
			log.error(msg, e)
		}
	}

	private Map<String, Object> createMessage(final EthereumAddress storageNodeAddress,
		final String streamId, final StreamStorageNode.AssigmentEvent eventType) {

		Map<String, Object> message = new HashMap<>()
		message.put("event", eventType.name())
		Map<String, Object> stream = new HashMap<>()
		stream.put("id", streamId)
		stream.put("partitions", streamService.getStream(streamId).getPartitions())
		message.put("stream", stream)
		message.put("storageNode", storageNodeAddress)
		return message
	}

	void removeStorageNodeFromStream(EthereumAddress storageNodeAddress, String streamId) {
		Stream stream = streamService.getStream(streamId)
		if (stream == null) {
			throw new NotFoundException("Stream", streamId)
		}
		StreamStorageNode instance = findStorageNodeByAddress(stream, storageNodeAddress)
		if (instance != null) {
			stream.removeFromStorageNodes(instance)
			instance.delete(flush: true)
			notifyStorageNode(storageNodeAddress, streamId, StreamStorageNode.AssigmentEvent.STREAM_REMOVED)
		} else {
			throw new NotFoundException("StorageNode", storageNodeAddress.toString())
		}
	}

	public static String createAssignmentStreamId() {
		EthereumAddress nodeAddress = EthereumAddress.fromPrivateKey(ApplicationConfig.getString("streamr.ethereum.nodePrivateKey"))
		return nodeAddress.toString() + "/storage-node-assignments"
	}
}
