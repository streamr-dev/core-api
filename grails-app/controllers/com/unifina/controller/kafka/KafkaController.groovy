package com.unifina.controller.kafka

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import org.apache.log4j.Logger

import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.task.Task
import com.unifina.feed.kafka.KafkaHistoricalFeed
import com.unifina.service.KafkaService

@Secured(["ROLE_ADMIN"])
class KafkaController {
	
	KafkaService kafkaService
	
	private static final Logger log = Logger.getLogger(KafkaController)
	
	def collect() {
		// Find all the defined Kafka Streams
		List<Stream> streams = Stream.withCriteria {
			feed {
				eq("backtestFeed", KafkaHistoricalFeed.class.name)
			}
			
			if (params.feed) {
				eq("feed", Feed.load(params.long("feed")))
			}
			if (params.stream) {
				eq("id", params.stream)
			}
		}
		List<Task> tasks = []
		// Create collect tasks
		streams.each {Stream stream->
			log.info("Creating Kafka collect tasks for stream $stream.name")
			tasks.addAll(kafkaService.createCollectTasks(stream))
		}
		
		render tasks as JSON
	}
	
}
