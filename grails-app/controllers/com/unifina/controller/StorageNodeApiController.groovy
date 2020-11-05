package com.unifina.controller

import com.unifina.domain.*
import com.unifina.service.*
import grails.converters.JSON
import grails.validation.Validateable
import org.apache.log4j.Logger

@Validateable
class StorageNodeAddCommand {
	String address

	static constraints = {
		address(nullable: false, validator: EthereumAddressValidator.validate)
	}
}

class StorageNodeApiController {

	private static final Logger log = Logger.getLogger(StorageNodeApiController)

	StorageNodeService storageNodeService
	PermissionService permissionService

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def findStreamsByStorageNode(String storageNodeAddress) {
		List<Stream> streams = storageNodeService.findStreamsByStorageNode(new EthereumAddress(storageNodeAddress))
		return render(streams*.toSummaryMap() as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def findStorageNodesByStream(String streamId) {
		List<StreamStorageNode> streamStorageNodes = storageNodeService.findStorageNodesByStream(streamId)
		return render(streamStorageNodes*.toMap() as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def addStorageNodeToStream(StorageNodeAddCommand command, String streamId) {
		log.info("addStorageNodeToStream: storageNodeAddress=" + command.address + ", streamId=" + streamId)
		if (command.validate()) {
			if (checkEditPermission(streamId, request.apiUser)) {
				StreamStorageNode streamStorageNode = storageNodeService.addStorageNodeToStream(new EthereumAddress(command.address), streamId)
				return render(streamStorageNode.toMap() as JSON)
			} else {
				throw new NotPermittedException(request.apiUser.username, "Stream", streamId)
			}
		} else {
			throw new ValidationException(command.errors)
		}
	}

	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def removeStorageNodeFromStream(String storageNodeAddress, String streamId) {
		log.info("removeStorageNodeFromStream: storageNodeAddress=" + storageNodeAddress + ", streamId=" + streamId)
		if (checkEditPermission(streamId, request.apiUser)) {
			storageNodeService.removeStorageNodeFromStream(new EthereumAddress(storageNodeAddress), streamId)
			return render(status: 204)
		} else {
			throw new NotPermittedException(request.apiUser.username, "Stream", streamId)
		}
	}

	private boolean checkEditPermission(String streamId, User user) {
		return permissionService.check(user, Stream.get(streamId), Permission.Operation.STREAM_EDIT)
	}
}
