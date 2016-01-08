package com.unifina.controller.api

import com.unifina.domain.data.Stream
import com.unifina.security.StreamrApi
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class StreamApiController {

	def streamService
	def unifinaSecurityService

	@StreamrApi
	def index() {
		def streams
		if (request.name) {
			streams = Stream.findAllByUserAndName(request.apiUser, request.name)
		} else {
			streams = Stream.findAllByUser(request.apiUser)
		}
		render(streams.collect { it.toMap() } as JSON)
	}

	@StreamrApi
	def save() {
		Stream stream
		if (request.JSON.fields) {
			stream = streamService.createUserStream(request.JSON, request.apiUser, request.JSON.fields)
		} else {
			stream = streamService.createUserStream(request.JSON, request.apiUser)
		}
		if (stream.hasErrors()) {
			log.info(stream.errors)
			render(status: 400, text: [success: false, error: "validation error", details: stream.errors] as JSON)
		} else {
			render(stream.toMap() as JSON)
		}
	}


	@StreamrApi
	def show() {
		getAuthorizedStream(params.id) { Stream stream ->
			render(stream.toMap() as JSON)
		}
	}

	@StreamrApi
	def delete() {
		getAuthorizedStream(params.id) { Stream stream ->
			stream.delete()
			render(status: 204)
		}
	}

	private def getAuthorizedStream(long id, Closure<Stream> successHandler) {
		def stream = Stream.findById(id)
		if (stream == null) {
			render(status: 404, text: [error: "Stream not found with id " + id, code: "NOT_FOUND"] as JSON)
		} else if (!unifinaSecurityService.canAccess(stream, request.apiUser)) {
			render(status: 403, text: [error: "Not authorized to access Stream " + id, code: "FORBIDDEN"] as JSON)
		} else {
			successHandler.call(stream)
		}
	}
}
