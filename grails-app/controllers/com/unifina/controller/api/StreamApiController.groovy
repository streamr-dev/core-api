package com.unifina.controller.api

import com.unifina.api.ValidationException
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Permission.Operation
import com.unifina.security.StreamrApi
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class StreamApiController {

	def streamService
	def permissionService

	@StreamrApi
	def index() {
		def streams = permissionService.getAll(Stream, request.apiUser, Operation.READ, {
			if (params.name) {
				eq "name", params.name
			}
		})
		render(streams*.toMap() as JSON)
	}

	@StreamrApi
	def save() {
		Stream stream = streamService.createUserStream(request.JSON, request.apiUser, request.JSON.config?.fields)

		if (stream.hasErrors()) {
			throw new ValidationException(stream.errors)
		} else {
			render(stream.toMap() as JSON)
		}
	}


	@StreamrApi
	def show(String id) {
		getAuthorizedStream(id, Operation.READ) { Stream stream ->
			render(stream.toMap() as JSON)
		}
	}

	@StreamrApi
	def update(String id) {
		getAuthorizedStream(id, Operation.WRITE) { Stream stream ->
			Stream newStream = new Stream(request.JSON)
			stream.name = newStream.name
			stream.description = newStream.description
			stream.config = newStream.config
			if (stream.validate()) {
				stream.save(failOnError: true)
				render(status: 204)
			} else {
				throw new ValidationException(stream.errors)
			}
		}
	}

	@StreamrApi
	def delete(String id) {
		getAuthorizedStream(id, Operation.WRITE) { Stream stream ->
			stream.delete()
			render(status: 204)
		}
	}

	private def getAuthorizedStream(String uuid, Operation op, Closure successHandler) {
		def stream = Stream.findByUuid(uuid)
		if (stream == null) {
			render(status: 404, text: [error: "Stream not found with uuid " + uuid, code: "NOT_FOUND"] as JSON)
		} else if (!permissionService.check(request.apiUser, stream, op)) {
			render(status: 403, text: [error: "Not authorized to ${op.id} Stream " + uuid, code: "FORBIDDEN", fault: "op", op: op.id] as JSON)
		} else {
			successHandler.call(stream)
		}
	}
}
