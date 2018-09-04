package com.unifina.controller.api

import com.unifina.api.*
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Permission.Operation
import com.unifina.domain.security.SecUser
import com.unifina.feed.DataRange
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import com.unifina.utils.CSVImporter
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.web.multipart.MultipartFile

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class StreamApiController {

	static allowedMethods = [
		"setFields": "POST",
		"uploadCsvFile": "POST",
		"confirmCsvFileUpload": "POST"
	]

	def streamService
	def permissionService
	def apiService

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
		def stream = apiService.authorizedGetById(Stream, id, (SecUser) request.apiUser, Operation.WRITE)

		boolean deleteFileAfterwards = true
		File temporaryFile
		try {
			// Copy multipart contents to temporary file
			MultipartFile multipartFile = request.getFile("file")
			temporaryFile = File.createTempFile("csv_upload_", ".csv")
			multipartFile.transferTo(temporaryFile)

			Map config = (stream.config ? JSON.parse(stream.config) : [:])
			List fields = config.fields ? config.fields : []

			CSVImporter csv = new CSVImporter(temporaryFile, fields, null, null, (String) request.apiUser.timezone)
			if (csv.getSchema().timestampColumnIndex == null) {
				deleteFileAfterwards = false
				throw new CsvParseUnknownSchemaException(temporaryFile.getCanonicalPath(), (csv.getSchema().toMap() as JSON).toString())
			} else {
				Map updatedConfig = streamService.importCsv(csv, stream)
				stream.config = (updatedConfig as JSON)
				stream.save()
				render(status: 204)
			}
		} catch (Exception e) {
			Exception rootCause = ExceptionUtils.getRootCause(e)
			if (rootCause != null) {
				e = rootCause
			}
			log.error("Failed to import file", e)
			throw e
		} finally {
			if (deleteFileAfterwards && temporaryFile != null && temporaryFile.exists()) {
				temporaryFile.delete()
			}
		}
	}

	@StreamrApi
	def confirmCsvFileUpload(String id) {
		getAuthorizedStream(id, Operation.WRITE) { stream ->
			File file = new File(request.JSON.fileUrl)
			List fields = stream.config ? JSON.parse(stream.config).fields : []
			def index = Integer.parseInt(request.JSON.timestampColumnIndex)
			def format = request.JSON.dateFormat
			try {
				CSVImporter csv = new CSVImporter(file, fields, index, format)
				Map config = streamService.importCsv(csv, stream)
				stream.config = (config as JSON)
				stream.save()
				render(stream.toMap() as JSON)
			} catch (Throwable e) {
				e = ExceptionUtils.getRootCause(e)
				throw e
			}
		}
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
