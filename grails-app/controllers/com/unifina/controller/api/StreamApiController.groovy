package com.unifina.controller.api

import com.unifina.api.ApiException
import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.api.ValidationException
import com.unifina.feed.DataRange
import com.unifina.feed.mongodb.MongoDbConfig
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
		def streams = permissionService.get(Stream, request.apiUser) {

		}
		render(permissionService.getAll(Stream, user) {
			if (params.name) {
				eq "name", params.name
			}
			if (params.search) {
				or {
					like "name", "%${params.search}%"
					like "description", "%${params.search}%"
				}
			}
			order params.sort ?: "name", params.order ?: "asc"
			if (params.max) {
				maxResults params.max
			}
			firstResult params.offset ?: 0
		})
		render(streams*.toMap() as JSON)
	}

	@StreamrApi
	def save() {
		Stream stream = streamService.createStream(request.JSON, request.apiUser)
		render(stream.toMap() as JSON)
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
		getAuthorizedStream(id, Operation.READ) { Stream stream ->
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
		getAuthorizedStream(id, Operation.WRITE) { Stream stream ->
			stream.delete()
			render(status: 204)
		}
	}

	@StreamrApi
	def range(String id) {
		getAuthorizedStream(id, Operation.READ) { Stream stream ->
			DataRange dataRange = streamService.getDataRange(stream)
			Map dataRangeMap = [beginDate: dataRange?.beginDate, endDate: dataRange?.endDate]
			render dataRangeMap as JSON
		}
	}

	private def getAuthorizedStream(String id, Operation op, Closure action) {
		def stream = Stream.get(id)
		if (stream == null) {
			throw new NotFoundException("Stream", id)
		} else if (!permissionService.check(request.apiUser, stream, op)) {
			throw new NotPermittedException(request.apiUser?.username, "Stream", id, op.id)
		} else {
			action.call(stream)
		}
	}
}
