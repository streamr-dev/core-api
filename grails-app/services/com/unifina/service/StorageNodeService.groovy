package com.unifina.service


import com.unifina.domain.Stream
import com.unifina.domain.StreamStorageNode
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class StorageNodeService {

	List<Stream> findStreamsByStorageNode(String storageNodeAddress) {
		List<StreamStorageNode> items = StreamStorageNode.findAllByStorageNodeAddress(storageNodeAddress)
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

	StreamStorageNode addStorageNodeToStream(String storageNodeAddress, String streamId) {
		boolean exists = (StreamStorageNode.findByStorageNodeAddressAndStreamId(storageNodeAddress, streamId) != null)
		if (!exists) {
			StreamStorageNode instance = new StreamStorageNode(
				streamId: streamId,
				storageNodeAddress: storageNodeAddress
			)
			return instance.save(validate: true)
		} else {
			throw new DuplicateNotAllowedException("StorageNode", storageNodeAddress)
		}
	}

	void removeStorageNodeFromStream(String storageNodeAddress, String streamId) {
		StreamStorageNode instance = StreamStorageNode.findByStorageNodeAddressAndStreamId(storageNodeAddress, streamId)
		if (instance != null) {
			instance.delete()
		} else {
			throw new NotFoundException("StorageNode", storageNodeAddress)
		}
	}
}
