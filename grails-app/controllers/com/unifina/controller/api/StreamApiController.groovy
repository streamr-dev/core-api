package com.unifina.controller.api

import com.unifina.domain.data.Stream
import com.unifina.security.StreamrApi
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class StreamApiController {

	static namespace = "api-v1"

	def streamService

	@StreamrApi
	def index() {
		def streams
		if (request.name) {
			streams = Stream.findAllByUserAndName(request.apiUser, request.name)
		} else {
			streams = Stream.findAllByUser(request.apiUser)
		}
		render(streams as JSON)
	}

	@StreamrApi
	def create() {
		Stream stream = streamService.createUserStream(request.JSON, request.apiUser)
		if (stream.hasErrors()) {
			log.info(stream.errors)
			render (status:400, text: [success:false, error: "validation error", details: stream.errors] as JSON)
		}
		else {
			render ([success:true, stream:stream.uuid, auth:stream.apiKey, name:stream.name, description:stream.description, localId:stream.localId] as JSON)
		}
	}

	@StreamrApi
	def lookup() {
		Stream stream = Stream.findByUserAndLocalId(request.apiUser, request.JSON?.localId)
		if (!stream)
			render (status:404, text: [success:false, error: "stream not found"] as JSON)
		else render ([stream:stream.uuid] as JSON)
	}
}
