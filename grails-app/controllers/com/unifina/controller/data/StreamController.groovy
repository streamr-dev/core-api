package com.unifina.controller.data

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.domain.signalpath.Module
import com.unifina.utils.IdGenerator

@Secured(["ROLE_USER"])
class StreamController {

	def springSecurityService
	
	static defaultAction = "list"
	
	def unifinaSecurityService
	def feedFileService
	def kafkaService
	
	def beforeInterceptor = [action:{unifinaSecurityService.canAccess(Stream.get(params.long("id")))},
		except:['list','search','create']]
	
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
	
	def create() {
		if (request.method=="GET")
			[stream:new Stream()]
		else {
			Stream stream = new Stream(params)
			stream.uuid = IdGenerator.get()
			stream.apiKey = IdGenerator.get()
			stream.user = springSecurityService.currentUser			
			stream.localId = stream.name
			
			stream.feed = Feed.load(7)
			stream.streamConfig = ([fields:[], topic: stream.uuid] as JSON)
			
			if (!stream.validate()) {
				log.info(stream.errors)
				[stream:stream]
			}
			else {
				flash.message = "Your stream has been created! You can start pushing realtime messages to the API or upload a message history from a csv file."
				stream.save(flush:true, failOnError:true)
				redirect(action:"show", id:stream.id)
			}
		}
	}
	
	def edit() {
		// Access checked by beforeInterceptor
		Stream stream = Stream.get(params.id)
		[stream:stream, config:(stream.streamConfig ? JSON.parse(stream.streamConfig) : [:])]
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
		Set<Feed> allowedFeeds = springSecurityService.currentUser?.feeds ?: new HashSet<>()
		List<Map> streams = []

		if (!allowedFeeds.isEmpty()) {
			String hql = "select new map(s.id as id, s.name as name, s.feed.module.id as module, s.description as description) from Stream s "+
				"left outer join s.feed "+
				"left outer join s.feed.module "+
				"where (s.name like '"+params.term+"%' or s.description like '"+params.term+"%') "
				"and s.feed.id in ("+allowedFeeds.collect{ feed -> feed.id }.join(',')+") "

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
		def feedFiles = FeedFile.findAllByStream(stream, [sort:'beginDate'])
		return [feedFiles: feedFiles]
	}
	
	def upload() {
		// Access checked by beforeInspector
		Stream stream = Stream.get(params.id)
		def file = request.getFile("file")
		List fields = kafkaService.createFeedFilesFromCsv(file.inputStream, stream)
		
		Map config = (stream.streamConfig ? JSON.parse(stream.streamConfig) : [:])
		if (!config.fields || config.fields.isEmpty()) {
			config.fields = fields
			stream.streamConfig = (config as JSON)
		}
		
		render ([success:true] as JSON)
	}
	
}
