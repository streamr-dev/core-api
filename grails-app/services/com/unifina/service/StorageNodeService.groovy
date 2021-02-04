package com.unifina.service

import com.unifina.domain.Stream
import com.unifina.domain.StreamStorageNode
import com.unifina.domain.EthereumAddress
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class StorageNodeService {

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
			return instance.save(validate: true)
		} else {
			throw new DuplicateNotAllowedException("StorageNode", storageNodeAddress.toString())
		}
	}

	void removeStorageNodeFromStream(EthereumAddress storageNodeAddress, String streamId) {
		StreamStorageNode instance = StreamStorageNode.findByStorageNodeAddressAndStreamId(storageNodeAddress.toString(), streamId)
		if (instance != null) {
			instance.delete()
		} else {
			throw new NotFoundException("StorageNode", storageNodeAddress.toString())
		}
	}
}
