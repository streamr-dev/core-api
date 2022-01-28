package com.unifina.controller

import com.unifina.domain.*
import com.unifina.domain.Permission.Operation
import com.unifina.service.*
import grails.converters.JSON

class StreamApiController {
	StreamService streamService
	PermissionService permissionService
	ApiService apiService
	EnsService ensService
	EthereumUserService ethereumUserService

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def index(StreamListParams listParams) {
		if (params.public) {
			listParams.publicAccess = params.boolean("public")
		}
		def results = apiService.list(Stream, listParams, (User) request.apiUser)
		PaginationUtils.setHint(response, listParams, results.size(), params)
		if (params.noConfig) {
			render(results*.toSummaryMap() as JSON)
			return
		}
		render(results*.toMap() as JSON)
	}

	@StreamrApi
	def save(CreateStreamCommand cmd) {
		CustomStreamIDValidator streamIdValidator = new CustomStreamIDValidator(this.&isValidCustomStreamIDDomain)
		Stream stream = streamService.createStream(cmd, request.apiUser, streamIdValidator)
		render(stream.toMap() as JSON)
	}

	private boolean isValidCustomStreamIDDomain(String domain, User creator) {
		boolean isEthereumAddress = EthereumAddressValidator.validate(domain)
		if (isEthereumAddress) {
			return creator.equals(ethereumUserService.getEthereumUser(domain))
		} else {
			return ensService.isENSOwnedBy(domain, creator)
		}
	}

	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def show(String id) {
		def stream = Stream.get(id)
		if (stream == null) {
			throw new NotFoundException("Stream", id)
		}

		Userish userish = request.apiUser
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
			if (newStream.migrateToBrubeck == true) {
				stream.migrateToBrubeck = true
				stream.migrateSyncTurnedOnAt = new Date()
			}
			if (stream.validate()) {
				stream.save(failOnError: true)
				render(stream.toMap() as JSON)
			} else {
				throw new ValidationException(stream.errors)
			}
		}
	}


	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def setFields(String id) {
		Stream stream = apiService.authorizedGetById(Stream, id, request.apiUser, Operation.STREAM_EDIT)
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
			// Use zero address to mark public permissions
			if (address == EthereumAddress.ZERO) {
				if (permissionService.checkAnonymousAccess(stream, Operation.STREAM_PUBLISH)) {
					render(status: 200)
				} else {
					render(status: 404)
				}
				return
			}
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
			// Use zero address to mark public permissions
			if (address == EthereumAddress.ZERO) {
				if (permissionService.checkAnonymousAccess(stream, Operation.STREAM_SUBSCRIBE)) {
					render(status: 200)
				} else {
					render(status: 404)
				}
				return
			}
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
}
