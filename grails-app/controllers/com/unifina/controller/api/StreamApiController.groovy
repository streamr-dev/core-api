package com.unifina.controller.api

import com.unifina.domain.data.Stream
import com.unifina.security.StreamrApi
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class StreamApiController {

	def streamService

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
}
