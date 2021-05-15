package com.unifina.service

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
		List<StreamStorageNode> items = StreamStorageNode.findAllByStorageNodeAddress(storageNodeAddress.toString())
		Iterable<Serializable> streamIds = items.collect { it.streamId } as Iterable<Serializable>
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

	private static final class StorageNodeThreadErrorHandler implements Thread.UncaughtExceptionHandler {
		@Override
		void uncaughtException(Thread t, Throwable e) {
			String s = String.format("error while processing storage node request: %s", t.getName())
			log.error(s, e)
		}
	}

	StreamStorageNode addStorageNodeToStream(EthereumAddress storageNodeAddress, String streamId) {
		boolean exists = (StreamStorageNode.findByStorageNodeAddressAndStreamId(storageNodeAddress.toString(), streamId) != null)
		if (!exists) {
			StreamStorageNode instance = new StreamStorageNode(streamService.getStream(streamId), storageNodeAddress.toString())
			try {
				StreamStorageNode saved = instance.save(validate: true, failOnError: true)
				NotifyStorageNodeTask task = new NotifyStorageNodeTask(
					storageNodeAddress,
					streamId,
					NotifyStorageNodeTask.AssigmentEvent.STREAM_ADDED,
					streamService,
					streamrClientService)
				Thread thread = new Thread(task)
				thread.setUncaughtExceptionHandler(new StorageNodeThreadErrorHandler())
				thread.setName(String.format("AddStorageNodeTask[%s,%s]", streamId, storageNodeAddress))
				thread.start()
				return saved
			} catch (Exception e) {
				log.error("error while adding a new storage node to stream", e)
			}
		} else {
			throw new DuplicateNotAllowedException("StorageNode", storageNodeAddress.toString())
		}
	}

	void removeStorageNodeFromStream(EthereumAddress storageNodeAddress, String streamId) {
		StreamStorageNode instance = StreamStorageNode.findByStorageNodeAddressAndStreamId(storageNodeAddress.toString(), streamId)
		if (instance != null) {
			try {
				instance.delete(flush: true)
				NotifyStorageNodeTask task = new NotifyStorageNodeTask(
					storageNodeAddress,
					streamId,
					NotifyStorageNodeTask.AssigmentEvent.STREAM_REMOVED,
					streamService,
					streamrClientService)
				Thread thread = new Thread(task)
				thread.setUncaughtExceptionHandler(new StorageNodeThreadErrorHandler())
				thread.setName(String.format("RemoveStorageNodeTask[%s,%s]", streamId, storageNodeAddress))
				thread.start()
			} catch (Exception e) {
				log.error("error while removing a storage node from stream", e)
			}
		} else {
			throw new NotFoundException("StorageNode", storageNodeAddress.toString())
		}
	}

	public static String createAssignmentStreamId() {
		EthereumAddress nodeAddress = EthereumAddress.fromPrivateKey(ApplicationConfig.getString("streamr.ethereum.nodePrivateKey"))
		return nodeAddress.toString() + "/storage-node-assignments"
	}
}
