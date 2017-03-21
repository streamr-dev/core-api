package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.api.SaveKeyCommand
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import com.unifina.service.PermissionService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class KeyApiController {

	PermissionService permissionService

	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def save(SaveKeyCommand saveKeyCommand) {
		if (saveKeyCommand.username != null && saveKeyCommand.streamId != null) {
			throw new ApiException(422, "INVALID_ARGUMENT_COMBINATION", "cannot provide both username and streamId")
		} else if (saveKeyCommand.username == null && saveKeyCommand.streamId == null) {
			throw new ApiException(422, "INVALID_ARGUMENT_COMBINATION", "must provide either username or streamId")
		}

		Key key
		if (saveKeyCommand.username) {
			key = saveUserLinkedKey(saveKeyCommand)
		} else {
			key = saveAnonymousKeyAndLinkToStream(saveKeyCommand)
		}

		render(key.toMap() as JSON)
	}

	private Key saveAnonymousKeyAndLinkToStream(SaveKeyCommand saveKeyCommand) {
		Stream stream = Stream.get(saveKeyCommand.streamId)
		if (stream == null) {
			throw new NotFoundException("Stream not found", Stream.class.toString(), saveKeyCommand.streamId)
		}

		Key key = new Key()
		key.name = saveKeyCommand.name
		key.save(failOnError: true, validate: true)

		permissionService.grant(request.apiUser, stream, key, Permission.Operation.READ, false)
		return key
	}

	private Key saveUserLinkedKey(SaveKeyCommand saveKeyCommand) {
		SecUser user = SecUser.findByUsername(saveKeyCommand.username)

		if (user == null) {
			throw new NotFoundException("User not found", SecUser.class.toString(), saveKeyCommand.username)
		} else if (request.apiUser != user) {
			throw new NotPermittedException(request.apiUser?.username, "User", user.id.toString(), Permission.Operation.SHARE.toString())
		}

		Key key = new Key()
		key.name = saveKeyCommand.name
		key.user = user
		key.save(failOnError: true, validate: true)
		return key
	}
}
