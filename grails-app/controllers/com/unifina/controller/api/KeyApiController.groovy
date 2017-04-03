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
import groovy.transform.CompileStatic

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class KeyApiController {

	PermissionService permissionService

	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def saveUserKey() {
		Key key = new Key()
		key.name = params.name
		key.user = request.apiUser
		key.save(failOnError: true, validate: true)
		render(key.toMap() as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def saveStreamKey(String id) {
		Stream stream = Stream.get(id)
		if (stream == null) {
			throw new NotFoundException("Stream not found", Stream.class.toString(), id)
		}

		Key key = new Key()
		key.name = params.name
		key.save(failOnError: true, validate: true)

		Permission.Operation operation = Permission.Operation.fromString(params.permission)
		permissionService.grant(request.apiUser, stream, key, operation, false)

		Map response = key.toMap()
		response["permission"] = params.permission
		render(response as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def delete(String id) {
		Key key = Key.get(id)
		if (key == null) {
			throw new NotFoundException(Key.toString(), id)
		} else if (!canDeleteKey(key, request.apiUser)) {
			throw new NotPermittedException(request.apiUser.username, "Key", key.id.toString(), Permission.Operation.SHARE.toString())
		} else {
			def query = Permission.where {
				key == key
			}
			query.deleteAll()
			key.delete(flush: true)
			response.status = 204
			render ""
		}
	}

	@CompileStatic
	private boolean canDeleteKey(Key key, SecUser currentUser) {
		if (key.user != null) {
			return key.user == currentUser
		} else {
			List<Stream> streams = permissionService.get(Stream, currentUser, Permission.Operation.SHARE)
			for (Stream s : streams) {
				if (permissionService.canReadKey(key, s)) {
					return true
				}
			}
			return false
		}
	}
}
