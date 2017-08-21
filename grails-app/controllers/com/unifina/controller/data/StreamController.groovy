package com.unifina.controller.data

import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.Permission.Operation
import com.unifina.api.ApiException
import com.unifina.feed.DataRange
import com.unifina.feed.mongodb.MongoDbConfig
import com.unifina.feed.twitter.TwitterStreamConfig
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.transform.CompileStatic
import org.apache.commons.lang.exception.ExceptionUtils

import java.text.SimpleDateFormat

import org.springframework.web.multipart.MultipartFile

import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Module
import com.unifina.feed.DataRange
import com.unifina.feed.mongodb.MongoDbConfig
import com.unifina.utils.CSVImporter
import com.unifina.utils.CSVImporter.Schema
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.web.multipart.MultipartFile

import java.text.SimpleDateFormat

@Secured(["ROLE_USER"])
class StreamController {

	def springSecurityService
	
	static defaultAction = "list"
	
	def permissionService
	def streamService

	def list() {
		SecUser user = springSecurityService.currentUser
		List<Stream> streams = permissionService.get(Stream, user, {
			eq("uiChannel", false) // filter out UI channel Streams
			order("lastUpdated", "desc")
		})
		Set<Stream> shareable = permissionService.get(Stream, user, Operation.SHARE).toSet()
		Set<Stream> writable = permissionService.get(Stream, user, Operation.WRITE).toSet()
		[streams:streams, shareable:shareable, writable:writable, user:user]
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
		getAuthorizedStream(params.id) { stream, user ->
			boolean writetable = permissionService.canWrite(user, stream)
			boolean shareable = permissionService.canShare(user, stream)
			[stream: stream, writable: writetable, shareable: shareable]
		}
	}

	// Can be extended to handle more types
	def details() {
		getAuthorizedStream(params.id) { stream, user ->
			def model = [stream: stream, config: (stream.config ? JSON.parse(stream.config) : [:])]
			render(template: stream.feed.streamPageTemplate, model: model)
		}
	}

	def update() {
		getAuthorizedStream(params.id, Operation.WRITE) { stream, user ->
			stream.name = params.name
			stream.description = params.description
			stream.save(flush: true, failOnError: true)
			redirect(action: "show", id: stream.id)
		}
	}

	def configure() {
		getAuthorizedStream(params.id, Operation.WRITE) { stream, user ->
			[stream: stream, config: (stream.config ? JSON.parse(stream.config) : [:])]
		}
	}

	def edit() {
		getAuthorizedStream(params.id, Operation.WRITE) { stream, user ->
			[stream: stream, config: (stream.config ? JSON.parse(stream.config) : [:])]
		}
	}

	def configureMongo() {
		getAuthorizedStream(params.id, Operation.WRITE) { stream, user ->
			[stream: stream, mongo: MongoDbConfig.readFromStreamOrElseEmptyObject(stream)]
		}
	}

	// also callback from Sign in with Twitter
	def configureTwitterStream() {
		getAuthorizedStream(params.id, Operation.WRITE) { stream, user ->
			TwitterStreamConfig twitter = TwitterStreamConfig.forStream(stream, session)
			if (!twitter.accessToken && "oauth_verifier" in params) {
				twitter.setOAuthVerifier(params.oauth_verifier)
			}

			if (twitter.accessToken && twitter.accessTokenSecret) {
				return [stream: stream]
			} else {
				flash.message = "Twitter sign-in needs to be done before Twitter stream can be used!"
				redirect(action: "show")
			}
		}
	}

	// form action from configureTwitterStream
	def saveTwitterStream() {
		getAuthorizedStream(params.id, Operation.WRITE) { stream, user ->
			TwitterStreamConfig twitter = TwitterStreamConfig.forStream(stream, session)
			twitter.setKeywords(params.keywords as String)
			twitter.save()
			redirect(action: "show", id: stream.id)
		}
	}

	def delete() {
		getAuthorizedStream(params.id, Operation.WRITE) { stream, user ->
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
			getAuthorizedStream(params.id) { stream, user ->
				render((stream.config ? JSON.parse(stream.config).fields : []) as JSON)
			}
		} else if (request.method == "POST") {
			getAuthorizedStream(params.id, Operation.WRITE) { stream, user ->
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
		getAuthorizedStream(params.id) { stream, user ->
			DataRange dataRange = streamService.getDataRange(stream)
			return [dataRange: dataRange, stream:stream]
		}
	}
	
	def upload() {
		getAuthorizedStream(params.id, Operation.WRITE) { stream, user ->
			File temp
			boolean deleteFile = true
			try {
				MultipartFile file = request.getFile("file")
				temp = File.createTempFile("csv_upload_", ".csv")
				file.transferTo(temp)

				Map config = (stream.config ? JSON.parse(stream.config) : [:])
				List fields = config.fields ? config.fields : []
				CSVImporter csv = new CSVImporter(temp, fields, null, null, springSecurityService.currentUser.timezone)
				if (csv.getSchema().timestampColumnIndex == null) {
					deleteFile = false
					flash.message = "Unfortunately we couldn't recognize some of the fields in the CSV-file. But no worries! With a couple of confirmations we still can import your data."
					response.status = 500
					render([success: false, redirect: createLink(action: 'confirm', params: [id: params.id, file: temp.getCanonicalPath()])] as JSON)
				} else {
					Map updatedConfig = streamService.importCsv(csv, stream)
					stream.config = (updatedConfig as JSON)
					stream.save()
					render([success: true] as JSON)
				}
			} catch (Exception e) {
				Exception rootCause = ExceptionUtils.getRootCause(e)
				if(rootCause != null)
					e = rootCause
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
		getAuthorizedStream(params.id, Operation.WRITE) { stream, user ->
			File file = new File(params.file)

			Map config = stream.config ? JSON.parse(stream.config) : [:]
			List fields = config.fields ? config.fields : []

			CSVImporter csv = new CSVImporter(file, fields)
			Schema schema = csv.getSchema()

			[schema: schema, file: params.file, stream: stream]
		}
	}
	
	def confirmUpload() {
		getAuthorizedStream(params.id, Operation.WRITE) { stream, user ->
			File file = new File(params.file)
			List fields = stream.config ? JSON.parse(stream.config).fields : []
			def index = Integer.parseInt(params.timestampIndex)
			def format = params.customFormat ?: params.format
			try {
				CSVImporter csv = new CSVImporter(file, fields, index, format)
				Map config = streamService.importCsv(csv, stream)
				stream.config = (config as JSON)
				stream.save()
			} catch (Throwable e) {
				e = ExceptionUtils.getRootCause(e)
				flash.error = e.message
			}
			redirect(action: "show", id: params.id)
		}
	}

	def deleteDataUpTo() {
		getAuthorizedStream(params.id, Operation.WRITE) { Stream stream, SecUser user ->
			Date date = new SimpleDateFormat(message(code: "default.dateOnly.format")).parse(params.date) + 1
			streamService.deleteDataUpTo(stream, date)
			flash.message = "All data up to " + params.date + " successfully deleted"
			redirect(action: "show", params: [id: params.id])
		}
	}

	private def getAuthorizedStream(String id, Operation op=Operation.READ, Closure action) {
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
}
