package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.api.ValidationException
import com.unifina.feed.mongodb.MongoDbConfig
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
		if (params.name) {
			streams = Stream.findAllByUserAndName(request.apiUser, params.name)
		} else {
			streams = Stream.findAllByUser(request.apiUser)
		}
		render(streams*.toMap() as JSON)
	}

	@StreamrApi
	def save() {
		Stream stream = streamService.createStream(request.JSON, request.apiUser)
		render(stream.toMap() as JSON)
	}


	@StreamrApi
	def show(String id) {
		getAuthorizedStream(id) { Stream stream ->
			render(stream.toMap() as JSON)
		}
	}

	@StreamrApi
	def update(String id) {
		Stream newStream = new Stream(request.JSON)
		getAuthorizedStream(id) { Stream stream ->
			stream.name = newStream.name
			stream.description = newStream.description
			stream.config = readConfig()
			if (stream.validate()) {
				stream.save(failOnError: true)
				render(status: 204)
			} else {
				throw new ValidationException(stream.errors)
			}
		}
	}

	@StreamrApi
	def detectFields(String id) {
		getAuthorizedStream(id) { Stream stream ->
			if (streamService.autodetectFields(stream, params.boolean("flatten", false))) {
				render(stream.toMap() as JSON)
			} else {
				throw new ApiException(500, "NO_FIELDS_FOUND", "No fields found for Stream (id=$stream.id)")
			}
		}
	}


	private String readConfig() {
		Map config = request.JSON.config
		if (config?.mongodb) {
			def configObject = new MongoDbConfig(config.mongodb)
			if (!configObject.validate()) {
				throw new ValidationException(configObject.errors)
			}
		}
		return config
	}

	@StreamrApi
	def delete(String id) {
		getAuthorizedStream(id) { Stream stream ->
			stream.delete()
			render(status: 204)
		}
	}

	private def getAuthorizedStream(String uuid, Closure successHandler) {
		def stream = Stream.findByUuid(uuid)
		if (stream == null) {
			render(status: 404, text: [error: "Stream not found with uuid " + uuid, code: "NOT_FOUND"] as JSON)
		} else if (!unifinaSecurityService.canAccess(stream, request.apiUser)) {
			render(status: 403, text: [error: "Not authorized to access Stream " + uuid, code: "FORBIDDEN"] as JSON)
		} else {
			successHandler.call(stream)
		}
	}
}
