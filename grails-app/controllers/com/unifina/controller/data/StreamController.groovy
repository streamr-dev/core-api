package com.unifina.controller.data

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import java.text.SimpleDateFormat

import org.springframework.web.multipart.MultipartFile

import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Module
import com.unifina.security.StreamrApi
import com.unifina.utils.CSVImporter
import com.unifina.utils.CSVImporter.Schema

@Secured(["ROLE_USER"])
class StreamController {

	def springSecurityService
	
	static defaultAction = "list"
	
	def unifinaSecurityService
	def feedFileService
	def kafkaService
	def streamService
	
	def beforeInterceptor = [action:{
			if (!unifinaSecurityService.canAccess(Stream.get(params.long("id")))) {
				if (request.xhr)
					redirect(controller:'login', action:'ajaxDenied')
				else
					redirect(controller:'login', action:'denied')
					
				return false
			}
			else return true
		},
		except:['list','search','create','apiCreate','apiLookup']]
	
	def list() {
		List<Stream> streams = Stream.findAllByUser(springSecurityService.currentUser)
		[streams:streams]
	}
	
	def show() {
		// Access checked by beforeInterceptor
		Stream stream = Stream.get(params.id)
		[stream:stream]
	}
	
	// Can be extended to handle more types
	def details() {
		// Access checked by beforeInterceptor
		Stream stream = Stream.get(params.id)
		def model = [stream:stream, config:(stream.streamConfig ? JSON.parse(stream.streamConfig) : [:])]
		
		// User streams
		if (stream.feed.id==7) {
			render(template:"userStreamDetails", model:model)
		}
		else render ""
	}

	def update() {
		Stream stream = Stream.get(params.id)
		stream.name = params.name
		stream.description = params.description
		stream.save(flush:true, failOnError:true)
		redirect(action: "show", id: stream.id)
	}
	
	def create() {
		if (request.method=="GET")
			[stream:new Stream()]
		else {
			SecUser user = springSecurityService.currentUser
			Stream stream = streamService.createUserStream(params, user)
			
			if (stream.hasErrors()) {
				log.info(stream.errors)
				return [stream:stream]
			}
			else {
				flash.message = "Your stream has been created! You can start pushing realtime messages to the API or upload a message history from a csv file."
				redirect(action:"show", id:stream.id)
			}
		}
	}
	
	@StreamrApi
	@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
	def apiCreate() {
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
	@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
	def apiLookup() {
		Stream stream = Stream.findByUserAndLocalId(request.apiUser, request.JSON?.localId)
		if (!stream)
			render (status:404, text: [success:false, error: "stream not found"] as JSON)
		else render ([stream:stream.uuid] as JSON)
	}
	
	def configure() {
		// Access checked by beforeInterceptor
		Stream stream = Stream.get(params.id)
		[stream:stream, config:(stream.streamConfig ? JSON.parse(stream.streamConfig) : [:])]
	}

	def edit() {
		// Access checked by beforeInterceptor
		Stream stream = Stream.get(params.id)
		[stream:stream, config:(stream.streamConfig ? JSON.parse(stream.streamConfig) : [:])]
	}
	
	def delete() {
		// Access checked by beforeInterceptor
		Stream streamInstance = Stream.get(params.id)
		def name = streamInstance.name
		def streamId = streamInstance.uuid
		try {
			streamService.deleteStream(streamInstance)
			flash.message = "The stream $name has been deleted."
			redirect(action:"list")
		} catch (Exception e) {
			flash.error = "An error occurred while deleting the stream!"
			redirect(action:"show", id:streamInstance.id)
		}
	}
	
	def fields() {
		// Access checked by beforeInterceptor
		Stream stream = Stream.get(params.id)
		Map config = (stream.streamConfig ? JSON.parse(stream.streamConfig) : [:])
		if (request.method=="GET") {
			render (config.fields ?: []) as JSON
		}
		else if (request.method=="POST") {
			def fields = request.JSON
			config.fields = fields
			stream.streamConfig = (config as JSON)
			flash.message = "Stream fields updated."
			
			Map result = [success:true, id:stream.id]
			render result as JSON
		}
	}
	
	def search() {
		List<Map> streams = []
		SecUser user = springSecurityService.currentUser
		Set<Feed> allowedFeeds = user.feeds ?: new HashSet<>()

		if (!allowedFeeds.isEmpty()) {
			String hql = "select new map(s.id as id, s.name as name, s.feed.module.id as module, s.description as description) from Stream s "+
				"left outer join s.feed "+
				"left outer join s.feed.module "+
				"where (s.name like '"+params.term+"%' or s.description like '%"+params.term+"%') "+
				"and s.feed.id in ("+allowedFeeds.collect{ feed -> feed.id }.join(',')+") "+
				"and (s.feed.id != 7 OR s.user.id = ${user.id}) " // Quick fix for CORE-452, needs proper ACL
				
				if (params.feed) {
					hql += " and s.feed.id="+Feed.load(params.feed).id
				}

				if (params.module) {
					hql += " and s.feed.module.id="+Module.load(params.module).id
				}

				hql += " order by length(s.name), s.id asc"
		
				streams = Stream.executeQuery(hql, [ max: 10 ])
		}

		render streams as JSON
	}

	
	def files() {
		// Access checked by beforeInspector
		Stream stream = Stream.get(params.id)
		def dataRange = streamService.getDataRange(stream)
		return [dataRange: dataRange, stream:stream]
	}
	
	def upload() {
		// Access checked by beforeInspector
		
		File temp
		boolean deleteFile = true
		try {
			Stream stream = Stream.get(params.id)
			MultipartFile file = request.getFile("file")
			temp = File.createTempFile("csv_upload_", ".csv")
			file.transferTo(temp)

			Map config = (stream.streamConfig ? JSON.parse(stream.streamConfig) : [:])
			List fields = config.fields ? config.fields : []

			CSVImporter csv = new CSVImporter(temp, fields)
			if (csv.getSchema().timestampColumnIndex==null) {
				flash.message = "Unfortunately we couldn't recognize some of the fields in the CSV-file. But no worries! With a couple of confirmations we still can import your data."
				response.status = 500
				render ([success:false, redirect:createLink(action:'confirm', params: [id:params.id, file:temp.getCanonicalPath()])] as JSON)
				deleteFile = false
			}
			else {
				importCsv(csv, stream)
				render ([success:true] as JSON)
			}
		} catch (Exception e) {
				flash.error = "An error occurred while handling file: $e"
				response.status = 500
				render ([success:false, error: e.toString()] as JSON)
		} finally {
			if (deleteFile && temp.exists())
				temp.delete()
		}
	}

	def confirm() {
		Stream stream = Stream.get(params.id)
		File file = new File(params.file)

		Map config = stream.streamConfig ? JSON.parse(stream.streamConfig) : [:]
		List fields = config.fields ? config.fields : []

		CSVImporter csv = new CSVImporter(file, fields)
		Schema schema = csv.getSchema()
		
		[schema:schema, file:params.file, stream:stream]
	}
	
	def confirmUpload() {
		Stream stream = Stream.get(params.id)
		File file = new File(params.file)

		Map config = stream.streamConfig ? JSON.parse(stream.streamConfig) : [:]
		List fields = config.fields ? config.fields : []

		def format
		def index
		if(params.customFormat)
			format = params.customFormat
		else format = params.format
		index = Integer.parseInt(params.timestampIndex)
		try {
			CSVImporter csv = new CSVImporter(file, fields, index, format)
			importCsv(csv, stream)
		} catch (Exception e) {
			flash.message = "The format of the timestamp is not correct"
		}
		redirect(action:"show", id:params.id)
	}
	
	private void importCsv(CSVImporter csv, Stream stream) {
		kafkaService.createFeedFilesFromCsv(csv, stream)
		
		// Autocreate the stream config based on fields in the csv schema
		Map config = (stream.streamConfig ? JSON.parse(stream.streamConfig) : [:])
		if (!config.fields || config.fields.isEmpty()) {
			List fields = []
			
			// The primary timestamp column is implicit, so don't include it in streamConfig
			for (int i=0;i<csv.schema.entries.length;i++) {
				if (i!=csv.getSchema().timestampColumnIndex) {
					CSVImporter.SchemaEntry e = csv.getSchema().entries[i]
					fields << [name:e.name, type:e.type]
				}
			}
			
			config.fields = fields
			stream.streamConfig = (config as JSON)
		}
	}
	
	def deleteFeedFilesUpTo() {
		// Access checked by beforeInspector
		def stream = Stream.get(params.id)
		
		def date = new SimpleDateFormat(message(code:"default.dateOnly.format")).parse(params.date) + 1
		def feedFiles = FeedFile.findAllByStreamAndEndDateLessThan(stream, date)
		feedFiles.each {
			feedFileService.deleteFile(it)
		}
		def deletedCount = FeedFile.executeUpdate("delete from FeedFile ff where ff.stream = :stream and ff.endDate < :date", [stream: stream, date: date])
		if(deletedCount > 0){
			flash.message = "All data up to "+params.date+" successfully deleted"
		} else {
			flash.error = "Something went wrong with deleting files"
		}

		redirect(action:"show", params:[id:params.id])
	}

	def getDataRange() {
		Stream stream = Stream.get(params.id)
		Map dataRange = streamService.getDataRange(stream)
		render dataRange as JSON
	}
	
}
