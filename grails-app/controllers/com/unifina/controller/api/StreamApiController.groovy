package com.unifina.controller.api

import com.unifina.api.*
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.security.SecUser
import com.unifina.feed.DataRange
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import com.unifina.service.StreamService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.apache.commons.lang.time.DateUtils
import org.springframework.web.multipart.MultipartFile

import java.text.SimpleDateFormat

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class StreamApiController {

	static allowedMethods = [
		"setFields": "POST",
		"uploadCsvFile": "POST",
		"confirmCsvFileUpload": "POST"
	]

	private final SimpleDateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
	private final SimpleDateFormat iso8601cal = new SimpleDateFormat("yyyy-MM-dd")

	def streamService
	def permissionService
	def apiService
	def csvUploadService

	@StreamrApi
	def index(StreamListParams listParams) {
		if (params.public) {
			listParams.publicAccess = params.boolean("public")
		}
		def results = apiService.list(Stream, listParams, (SecUser) request.apiUser)
		apiService.addLinkHintToHeader(listParams, results.size(), params, response)
		if (params.noConfig) {
			render(results*.toSummaryMap() as JSON)
			return
		}
		render(results*.toMap() as JSON)
	}

	@StreamrApi
	def save() {
		Stream stream = streamService.createStream(request.JSON, request.apiUser)
		render(stream.toMap() as JSON)
	}


	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def show(String id) {
		streamService.getReadAuthorizedStream(id, request.apiUser, request.apiKey) { Stream stream ->
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
			if (newStream.autoConfigure != null) {
				stream.autoConfigure = newStream.autoConfigure
			}
			if (newStream.requireSignedData != null) {
				stream.requireSignedData = newStream.requireSignedData
			}
			if (newStream.storageDays != null) {
				stream.storageDays = newStream.storageDays
			}
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

	@StreamrApi
	def setFields(String id) {
		def stream = apiService.authorizedGetById(Stream, id, (SecUser) request.apiUser, Operation.WRITE)
		def givenFields = request.JSON

		Map config = stream.config ? JSON.parse(stream.config) : [:]
		config.fields = givenFields

		stream.config = (config as JSON)
		stream.save(failOnError: true)

		render(stream.toMap() as JSON)
	}

	@StreamrApi
	def dataFiles(String id) {
		getAuthorizedStream(id) { stream ->
			DataRange dataRange = streamService.getDataRange(stream)
			render([dataRange: dataRange, stream:stream] as JSON)
		}
	}

	@StreamrApi
	def uploadCsvFile(String id) {
		// Copy multipart contents to temporary file
		MultipartFile multipartFile = request.getFile("file")
		File temporaryFile = File.createTempFile("csv_upload_", ".csv")
		multipartFile.transferTo(temporaryFile)

		try {
			def result = csvUploadService.uploadCsvFile(temporaryFile, id, (SecUser) request.apiUser)
			render(result as JSON)
		} catch (Exception e) {
			if (temporaryFile != null && temporaryFile.exists()) {
				temporaryFile.delete()
			}
			throw e
		}
	}

	@StreamrApi
	def confirmCsvFileUpload(String id, CsvParseInstructions instructions) {
		Stream stream = csvUploadService.parseAndConsumeCsvFile(instructions, id, (SecUser) request.apiUser)
		render(stream.toMap() as JSON)
	}

	private String readConfig() {
		Map config = request.JSON.config
		return config
	}

	@StreamrApi
	def delete(String id) {
		getAuthorizedStream(id, Operation.WRITE) { Stream stream ->
			streamService.deleteStream(stream)
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

	@StreamrApi
	def publishers(String id) {
		getAuthorizedStream(id, Operation.READ) { Stream stream ->
			Set<String> publisherAddresses = streamService.getStreamEthereumPublishers(stream)
			render([addresses: publisherAddresses] as JSON)
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

		int days = params.int("days", 2)
		Date threshold = DateUtils.addDays(new Date(), -days)
		StreamService.StreamStatus status = streamService.status(s, threshold)
		response.status = 200
		if (status.date == null) {
			render([ok: status.ok ] as JSON)
			return
		}
		render([
			ok: status.ok,
			date: iso8601.format(status.date),
		] as JSON)
	}

	@StreamrApi
	def deleteDataUpTo(String id) {
		getAuthorizedStream(id, Operation.WRITE) { Stream stream ->
			Date date = iso8601cal.parse((String) params.date)
			streamService.deleteDataUpTo(stream, date)
			render(status: 204)
		}
	}

	@StreamrApi
	def deleteAllData(String id) {
		getAuthorizedStream(id, Operation.WRITE) { Stream stream ->
			streamService.deleteAllData(stream)
			render(status: 204)
		}
	}

	@StreamrApi
	def deleteDataRange(String id) {
		getAuthorizedStream(id, Operation.WRITE) { Stream stream ->
			Date start = iso8601cal.parse((String) params.start)
			Date end = iso8601cal.parse((String) params.end)
			streamService.deleteDataRange(stream, start, end)
			render(status: 204)
		}
	}
}
