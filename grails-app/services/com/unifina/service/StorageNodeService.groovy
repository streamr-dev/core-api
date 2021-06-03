package com.unifina.service

import com.unifina.domain.EthereumAddress
import com.unifina.domain.Stream
import com.unifina.domain.StreamStorageNode
import com.unifina.utils.ApplicationConfig
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class StorageNodeService {
	StreamService streamService
	StreamrClientService streamrClientService

	List<Stream> findStreamsByStorageNode(EthereumAddress storageNodeAddress) {
		List<StreamStorageNode> nodes = StreamStorageNode.findAllByStorageNodeAddress(storageNodeAddress.toString())
		List<Stream> results = new ArrayList<>()
		for (StreamStorageNode node : nodes) {
			Stream stream = node.getStream()
			results.add(stream)
		}
		return results
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
			NotifyStorageNodeTask task = new NotifyStorageNodeTask(
				storageNodeAddress,
				streamId,
				NotifyStorageNodeTask.AssigmentEvent.STREAM_ADDED,
				streamService,
				streamrClientService)
			Thread thread = new Thread(task)
			thread.setUncaughtExceptionHandler(new NotifyStorageNodeTask.ErrorHandler())
			thread.setName(String.format("AddStorageNodeTask[%s,%s]", streamId, storageNodeAddress))
			thread.start()
		}
		return node
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
			NotifyStorageNodeTask task = new NotifyStorageNodeTask(
				storageNodeAddress,
				streamId,
				NotifyStorageNodeTask.AssigmentEvent.STREAM_REMOVED,
				streamService,
				streamrClientService)
			Thread thread = new Thread(task)
			thread.setUncaughtExceptionHandler(new NotifyStorageNodeTask.ErrorHandler())
			thread.setName(String.format("RemoveStorageNodeTask[%s,%s]", streamId, storageNodeAddress))
			thread.start()
		} else {
			throw new NotFoundException("StorageNode", storageNodeAddress.toString())
		}
	}

	public static String createAssignmentStreamId() {
		EthereumAddress nodeAddress = EthereumAddress.fromPrivateKey(ApplicationConfig.getString("streamr.ethereum.nodePrivateKey"))
		return nodeAddress.toString() + "/storage-node-assignments"
	}
}
