package com.unifina.controller.data

import com.unifina.domain.security.Permission.Operation
import com.unifina.api.ApiException
import com.unifina.feed.DataRange
import com.unifina.feed.mongodb.MongoDbConfig
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import java.text.SimpleDateFormat

import org.springframework.web.multipart.MultipartFile

import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Module
import com.unifina.utils.CSVImporter
import com.unifina.utils.CSVImporter.Schema

@Secured(["ROLE_USER"])
class StreamController {

	def springSecurityService
	
	static defaultAction = "list"
	
	def permissionService
	def feedFileService
	def kafkaService
	def streamService

	private def getAuthorizedStream(long id, Operation op=Operation.READ, Closure action) {
		SecUser user = springSecurityService.currentUser
		Stream stream = Stream.get(id)
		if (!stream) {
			response.sendError(404)
		} else if (!permissionService.check(user, stream, op)) {
			redirect controller: 'login', action: (request.xhr ? 'ajaxDenied' : 'denied')
		} else {
			action.call(stream, user)
		}
	}

	def list() {
		SecUser user = springSecurityService.currentUser
		List<Stream> streams = permissionService.get(Stream, user)
		List<Stream> shareable = permissionService.get(Stream, user, Operation.SHARE)
		[streams:streams, shareable:shareable]
	}

	def search() {
		SecUser user = springSecurityService.currentUser
		render(permissionService.getAll(Stream, user) {
			or {
				like "name", "%${params.term}%"
				like "description", "%${params.term}%"
			}
			order "name", "asc"
			maxResults 10
		}.collect { stream -> [
			id: stream.id,
			name: stream.name,
			description: stream.description,
			module: stream.feed.moduleId
		]} as JSON)
	}

	def create() {
		SecUser user = springSecurityService.currentUser

		boolean justStarted = request.method == "GET"
		Stream stream = justStarted ? new Stream() : streamService.createStream(params, user);

		if (!justStarted && stream.hasErrors()) { log.info(stream.errors) }
		if (justStarted || stream.hasErrors()) {
			return [
				stream: stream,
				feeds: permissionService.get(Feed, user),
				defaultFeed: Feed.findById(Feed.KAFKA_ID)
			]
		}

		flash.message = "Your stream has been created! " +
				"You can start pushing realtime messages to the API or upload a message history from a csv file."
		redirect(action: "show", id: stream.id)
	}

	def show() {
		getAuthorizedStream(params.long("id")) { stream, user ->
			[stream: stream, shareable: permissionService.canShare(user, stream)]
		}
	}

	// Can be extended to handle more types
	def details() {
		getAuthorizedStream(params.long("id")) { stream, user ->
			def model = [stream: stream, config: (stream.config ? JSON.parse(stream.config) : [:])]
			render(template: stream.feed.streamPageTemplate, model: model)
		}
	}

	def update() {
		getAuthorizedStream(params.long("id"), Operation.WRITE) { stream, user ->
			stream.name = params.name
			stream.description = params.description
			stream.save(flush: true, failOnError: true)
			redirect(action: "show", id: stream.id)
		}
	}

	def configure() {
		getAuthorizedStream(params.long("id"), Operation.WRITE) { stream, user ->
			[stream: stream, config: (stream.config ? JSON.parse(stream.config) : [:])]
		}
	}

	def edit() {
		getAuthorizedStream(params.long("id"), Operation.WRITE) { stream, user ->
			[stream: stream, config: (stream.config ? JSON.parse(stream.config) : [:])]
		}
	}

	def configureMongo() {
		Stream stream = Stream.get(params.id)
		[stream: stream, mongo: MongoDbConfig.readFromStreamOrElseEmptyObject(stream)]
	}

	def delete() {
		getAuthorizedStream(params.long("id"), Operation.WRITE) { stream, user ->
			try {
				streamService.deleteStream(stream)
				flash.message = "The stream $stream.name has been deleted."
				redirect(action: "list")
			} catch (Exception e) {
				flash.error = "An error occurred while deleting the stream!"
				redirect(action: "show", id: stream.id)
			}
		}
	}
	
	def fields() {
		if (request.method == "GET") {
			getAuthorizedStream(params.long("id")) { stream, user ->
				render((stream.config ? JSON.parse(stream.config).fields : []) as JSON)
			}
		} else if (request.method == "POST") {
			getAuthorizedStream(params.long("id"), Operation.WRITE) { stream, user ->
				def fields = request.JSON
				Map config = stream.config ? JSON.parse(stream.config) : [:]
				config.fields = fields
				stream.config = (config as JSON)
				flash.message = "Stream fields updated."

				Map result = [success: true, id: stream.id]
				render result as JSON
			}
		}
	}
	
	def files() {
		getAuthorizedStream(params.long("id")) { stream, user ->
			DataRange dataRange = streamService.getDataRange(stream)
			return [dataRange: dataRange, stream:stream]
		}
	}
	
	def upload() {
		getAuthorizedStream(params.long("id"), Operation.WRITE) { stream, user ->
			File temp
			boolean deleteFile = true
			try {
				MultipartFile file = request.getFile("file")
				temp = File.createTempFile("csv_upload_", ".csv")
				file.transferTo(temp)

				Map config = (stream.config ? JSON.parse(stream.config) : [:])
				List fields = config.fields ? config.fields : []

				CSVImporter csv = new CSVImporter(temp, fields)
				if (csv.getSchema().timestampColumnIndex == null) {
					deleteFile = false
					flash.message = "Unfortunately we couldn't recognize some of the fields in the CSV-file. But no worries! With a couple of confirmations we still can import your data."
					response.status = 500
					render([success: false, redirect: createLink(action: 'confirm', params: [id: params.id, file: temp.getCanonicalPath()])] as JSON)
				} else {
					importCsv(csv, stream)
					render([success: true] as JSON)
				}
			} catch (Exception e) {
				log.error("Failed to import file", e)
				response.status = 500
				render([success: false, error: e.message] as JSON)
			} finally {
				if (deleteFile && temp != null && temp.exists()) {
					temp.delete()
				}
			}
		}
	}

	def confirm() {
		getAuthorizedStream(params.long("id"), Operation.WRITE) { stream, user ->
			File file = new File(params.file)

			Map config = stream.config ? JSON.parse(stream.config) : [:]
			List fields = config.fields ? config.fields : []

			CSVImporter csv = new CSVImporter(file, fields)
			Schema schema = csv.getSchema()

			[schema: schema, file: params.file, stream: stream]
		}
	}
	
	def confirmUpload() {
		getAuthorizedStream(params.long("id"), Operation.WRITE) { stream, user ->
			File file = new File(params.file)
			List fields = stream.config ? JSON.parse(stream.config).fields : []
			def index = Integer.parseInt(params.timestampIndex)
			def format = params.customFormat ?: params.format
			try {
				CSVImporter csv = new CSVImporter(file, fields, index, format)
				importCsv(csv, stream)
			} catch (Exception e) {
				flash.message = "The format of the timestamp is not correct"
			}
			redirect(action: "show", id: params.id)
		}
	}

	private void importCsv(CSVImporter csv, Stream stream) {
		kafkaService.createFeedFilesFromCsv(csv, stream)
		
		// Autocreate the stream config based on fields in the csv schema
		Map config = (stream.config ? JSON.parse(stream.config) : [:])

		List fields = []

		// The primary timestamp column is implicit, so don't include it in streamConfig
		for (int i=0;i<csv.schema.entries.length;i++) {
			if (i!=csv.getSchema().timestampColumnIndex) {
				CSVImporter.SchemaEntry e = csv.getSchema().entries[i]
				if (e!=null)
					fields << [name:e.name, type:e.type]
			}
		}

		config.fields = fields
		stream.config = (config as JSON)
	}
	
	def deleteFeedFilesUpTo() {
		getAuthorizedStream(params.long("id"), Operation.WRITE) { stream, user ->
			def date = new SimpleDateFormat(message(code: "default.dateOnly.format")).parse(params.date) + 1
			FeedFile.findAllByStreamAndEndDateLessThan(stream, date).each {
				feedFileService.deleteFile(it)
			}
			def deletedCount = FeedFile.executeUpdate("delete from FeedFile ff where ff.stream = :stream and ff.endDate < :date", [stream: stream, date: date])
			if (deletedCount > 0) {
				flash.message = "All data up to " + params.date + " successfully deleted"
			} else {
				flash.error = "Something went wrong with deleting files"
			}
			redirect(action: "show", params: [id: params.id])
		}
	}

	def getDataRange() {
		Stream stream = Stream.get(params.id)
		DataRange dataRange = streamService.getDataRange(stream)
		Map dataRangeMap = [beginDate: dataRange?.beginDate, endDate: dataRange?.endDate]
		render dataRangeMap as JSON
	}

}
