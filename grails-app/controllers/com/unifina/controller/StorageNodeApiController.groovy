package com.unifina.controller

import com.unifina.security.StreamrApi
import com.unifina.security.AuthLevel
import com.unifina.domain.Stream
import com.unifina.domain.StreamStorageNode
import com.unifina.service.StorageNodeService
import com.unifina.service.PermissionService
import com.unifina.api.ValidationException
import com.unifina.api.NotPermittedException
import com.unifina.utils.EthereumAddressValidator
import com.unifina.domain.Permission
import com.unifina.domain.User
import grails.converters.JSON
import grails.validation.Validateable

@Validateable
class StorageNodeAddCommand {
	String address

	static constraints = {
		address(nullable: false, validator: EthereumAddressValidator.validate)
	}
}

class StorageNodeApiController {

	StorageNodeService storageNodeService
	PermissionService permissionService

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def findStreamsByStorageNode(String storageNodeAddress) {
		List<Stream> streams = storageNodeService.findStreamsByStorageNode(storageNodeAddress);
		return render(streams*.toSummaryMap() as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def findStorageNodesByStream(String streamId) {
		List<StreamStorageNode> streamStorageNodes = storageNodeService.findStorageNodesByStream(streamId);
		return render(streamStorageNodes*.toMap() as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def addStorageNodeToStream(StorageNodeAddCommand command, String streamId) {
		println("StorageNodeApiController.addStorageNodeToStream: storageNodeAddress=" + command.address + ", streamId=" + streamId)
		if (command.validate()) {
			if (checkEditPermission(streamId, request.apiUser)) {
				StreamStorageNode streamStorageNode = storageNodeService.addStorageNodeToStream(command.address, streamId)
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
		println("StorageNodeApiController.removeStorageNodeFromStream: storageNodeAddress=" + storageNodeAddress + ", streamId=" + streamId)
		if (checkEditPermission(streamId, request.apiUser)) {
			storageNodeService.removeStorageNodeFromStream(storageNodeAddress, streamId)
			return render(status: 204)
		} else {
			throw new NotPermittedException(request.apiUser.username, "Stream", streamId)
		}
	}

	private boolean checkEditPermission(String streamId, User user) {
		return permissionService.check(user, Stream.get(streamId), Permission.Operation.STREAM_EDIT)
	}
}