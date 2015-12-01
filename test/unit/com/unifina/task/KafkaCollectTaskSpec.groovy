
package com.unifina.task

import grails.converters.JSON
import grails.test.mixin.*
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.web.ControllerUnitTestMixin

import java.nio.file.Files
import java.nio.file.Path

import org.codehaus.groovy.grails.commons.GrailsApplication

import spock.lang.Specification

import com.unifina.domain.data.Stream
import com.unifina.domain.task.Task
import com.unifina.kafkaclient.UnifinaKafkaIterator
import com.unifina.kafkaclient.UnifinaKafkaMessage
import com.unifina.service.BootService
import com.unifina.service.FeedFileService


@TestMixin(ControllerUnitTestMixin) // to get JSON converter
@Mock([Stream])
class KafkaCollectTaskSpec extends Specification {

	static byte CONTENT_TYPE_STRING = 11
	
	def iteratorMock
	
    def setup() {
		BootService.mergeDefaultConfig(grailsApplication)
		
		defineBeans {
			feedFileService(FeedFileService)
		}
		
		iteratorMock = mockFor(UnifinaKafkaIterator)
		
		iteratorMock.demand.hasNext {-> true }
		iteratorMock.demand.next {-> 
			return new UnifinaKafkaMessage("KafkaCollectTaskSpec", "KafkaCollectTaskSpec", System.currentTimeMillis(), CONTENT_TYPE_STRING, "foo".getBytes("UTF-8")) 
		}
    }

    def cleanup() {

    }

	void "test collecting a date range of events from Kafka"() {
		// Create Stream
		Stream stream = new Stream(uuid: 'KafkaCollectTaskSpec')
		stream.streamConfig = ([topic:stream.uuid] as JSON)
		stream.save(validate:false, flush:true, failOnError:true)
		
		// Produce test data to Kafka
		long beginDate = System.currentTimeMillis()
		// TODO: produce events to Kafka
		long endDate = System.currentTimeMillis()
		
		// Create the instance to be tested
		KafkaCollectTask task = new KafkaCollectTask(new Task(), [streamId: stream.id, beginDate: beginDate, endDate: endDate, filename: "KafkaCollectTaskSpec.tmp"], grailsApplication) {
			// Return mock Kafka iterator instead of a real one
			@Override
			protected UnifinaKafkaIterator createIterator(String arg0, Date arg1, Date arg2) {
				return iteratorMock.createMock()
			}
		}
		final Path writtenFile = Files.createTempFile("KafkaCollectTaskSpec", "tmp")
		
		// Mock the FeedFileService
		FeedFileService feedFileService = [createFeedFile: {Stream s, Date bd, Date ed, File file, boolean overwriteExisting->
			// The file will be deleted after calling this service, so let's make a copy of it for inspection
			Files.copy(file.toPath(), writtenFile)
		}] as FeedFileService
		task.feedFileService = feedFileService
		
		when: "the task is run"
			task.run()
		then: "the written file should contain the correct events"
			writtenFile.toFile().exists()
			writtenFile.toFile().length() > 0
			
	}

}
