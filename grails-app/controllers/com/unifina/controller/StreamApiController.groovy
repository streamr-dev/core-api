package com.unifina.controller

import com.unifina.domain.*
import com.unifina.domain.Permission.Operation
import com.unifina.feed.DataRange
import com.unifina.service.*
import grails.converters.JSON

import java.text.SimpleDateFormat

class StreamApiController {
	StreamService streamService
	PermissionService permissionService
	ApiService apiService
	EnsService ensService

	@StreamrApi
	def index(StreamListParams listParams) {
		if (params.public) {
			listParams.publicAccess = params.boolean("public")
		}
		def results = apiService.list(Stream, listParams, (User) request.apiUser)
		apiService.addLinkHintToHeader(listParams, results.size(), params, response)
		if (params.noConfig) {
			render(results*.toSummaryMap() as JSON)
			return
		}
		render(results*.toMap() as JSON)
	}

	@StreamrApi
	def save(CreateStreamCommand cmd) {
		CustomStreamIDValidator streamIdValidator = new CustomStreamIDValidator({domain, creator -> ensService.isENSOwnedBy(domain, creator)})
		Stream stream = streamService.createStream(cmd, request.apiUser, streamIdValidator)
		render(stream.toMap() as JSON)
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def show(String id) {
		def stream = Stream.get(id)
		if (stream == null) {
			throw new NotFoundException("Stream", id)
		}

		Userish userish = request.apiKey ?: request.apiUser
		if (permissionService.check(userish, stream, Permission.Operation.STREAM_GET)) {
			render(stream.toMap() as JSON)
		} else {
			throw new NotPermittedException(request?.apiUser?.username, "Stream", id, Permission.Operation.STREAM_GET.id)
		}
	}

	@StreamrApi
	def update(String id) {
		getAuthorizedStream(id, Operation.STREAM_EDIT) { Stream stream ->
			Stream newStream = new Stream(request.JSON)
			stream.name = newStream.name
			stream.description = newStream.description
			stream.config = readConfig()
			if (newStream.partitions != null) {
				stream.partitions = newStream.partitions
			}
			if (newStream.autoConfigure != null) {
				stream.autoConfigure = newStream.autoConfigure
			}
			if (newStream.requireSignedData != null) {
				stream.requireSignedData = newStream.requireSignedData
			}
			if (newStream.requireEncryptedData != null) {
				stream.requireEncryptedData = newStream.requireEncryptedData
			}
			if (newStream.storageDays != null) {
				stream.storageDays = newStream.storageDays
			}
			if (newStream.inactivityThresholdHours != null) {
				stream.inactivityThresholdHours = newStream.inactivityThresholdHours
			}
			if (stream.validate()) {
				stream.save(failOnError: true)
				render(stream.toMap() as JSON)
			} else {
				throw new ValidationException(stream.errors)
			}
		}
	}

	@StreamrApi
	def detectFields(String id) {
		boolean saveFields = false
		if ("GET".equals(request.method)) {
			saveFields = false
		} else if ("POST".equals(request.method)) {
			saveFields = true
		}
		getAuthorizedStream(id, Operation.STREAM_EDIT) { Stream stream ->
			if (streamService.autodetectFields(stream, params.boolean("flatten", false), saveFields)) {
				render(stream.toMap() as JSON)
			} else {
				throw new ApiException(500, "NO_FIELDS_FOUND", "No fields found for Stream (id=$stream.id)")
			}
		}
	}

	@StreamrApi(authenticationLevel = AuthLevel.KEY)
	def setFields(String id) {
		Userish u = request.apiUser != null ? (User) request.apiUser : (Key) request.apiKey
		Stream stream = apiService.authorizedGetById(Stream, id, u, Operation.STREAM_EDIT)
		def givenFields = request.JSON

		Map config = stream.config ? JSON.parse(stream.config) : [:]
		config.fields = givenFields

		stream.config = (config as JSON)
		stream.save(failOnError: true)

		render(stream.toMap() as JSON)
	}


	private String readConfig() {
		Map config = request.JSON.config
		return config
	}

	@StreamrApi
	def delete(String id) {
		getAuthorizedStream(id, Operation.STREAM_DELETE) { Stream stream ->
			streamService.deleteStream(stream)
			render(status: 204)
		}
	}

	@StreamrApi
	def range(String id) {
		getAuthorizedStream(id, Operation.STREAM_GET) { Stream stream ->
			DataRange dataRange = streamService.getDataRange(stream)
			Map dataRangeMap = [beginDate: dataRange?.beginDate, endDate: dataRange?.endDate]
			render dataRangeMap as JSON
		}
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def validation(String id) {
		Stream stream = Stream.get(id)
		if (stream == null) {
			throw new NotFoundException("Stream", id)
		} else {
			render(stream.toValidationMap() as JSON)
		}
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def publishers(String id) {
		Stream stream = Stream.get(id)
		if (stream == null) {
			throw new NotFoundException("Stream", id)
		} else {
			Set<String> publisherAddresses = streamService.getStreamEthereumPublishers(stream)
			render([addresses: publisherAddresses] as JSON)
		}
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def subscribers(String id) {
		Stream stream = Stream.get(id)
		if (stream == null) {
			throw new NotFoundException("Stream", id)
		} else {
			Set<String> subscriberAddresses = streamService.getStreamEthereumSubscribers(stream)
			render([addresses: subscriberAddresses] as JSON)
		}
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def publisher(String id, String address) {
		Stream stream = Stream.get(id)
		if (stream == null) {
			throw new NotFoundException("Stream", id)
		} else {
			if (streamService.isStreamEthereumPublisher(stream, address)) {
				render(status: 200)
			} else {
				render(status: 404)
			}
		}
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def subscriber(String id, String address) {
		Stream stream = Stream.get(id)
		if (stream == null) {
			throw new NotFoundException("Stream", id)
		} else {
			if (streamService.isStreamEthereumSubscriber(stream, address)) {
				render(status: 200)
			} else {
				render(status: 404)
			}
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

	@StreamrApi
	def status() {
		Stream s = Stream.get((String) params.id)
		if (s == null) {
			response.status = 404
			throw new NotFoundException("Stream not found.", "Stream", (String) params.id)
		}

		StreamService.StreamStatus status = streamService.status(s, new Date())
		response.status = 200
		if (status.date == null) {
			render([ok: status.ok ] as JSON)
			return
		}
		SimpleDateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
		render([
			ok: status.ok,
			date: iso8601.format(status.date),
		] as JSON)
	}
}
