package com.unifina.controller.kafka

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured;

import org.apache.log4j.Logger

import com.unifina.domain.data.Stream
import com.unifina.domain.task.Task
import com.unifina.feed.kafka.KafkaHistoricalFeed
import com.unifina.service.KafkaService

class KafkaController {
	
	KafkaService kafkaService
	
	private static final Logger log = Logger.getLogger(KafkaController)
	
	@Secured(["ROLE_ADMIN"])
	def collect() {
		// Find all the defined Kafka Streams
		List<Stream> streams = Stream.withCriteria {
			feed {
				eq("backtestFeed", KafkaHistoricalFeed.class.name)
			}
		}
		List<Task> tasks = []
		// Create collect tasks
		streams.each {Stream stream->
			tasks.addAll(kafkaService.createCollectTasks(stream))
		}
		
		render tasks as JSON
	}
	
}
