package com.unifina.task

import grails.converters.JSON
import grails.test.mixin.*
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.web.ControllerUnitTestMixin

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.zip.GZIPInputStream

import org.codehaus.groovy.grails.commons.GrailsApplication

import spock.lang.Specification

import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.domain.task.Task
import com.unifina.feed.kafka.KafkaFeedFileWriter
import com.unifina.feed.kafka.KafkaHistoricalIterator
import com.unifina.kafkaclient.UnifinaKafkaIterator
import com.unifina.kafkaclient.UnifinaKafkaMessage
import com.unifina.service.BootService
import com.unifina.service.FeedFileService


@TestMixin(ControllerUnitTestMixin) // to get JSON converter
@Mock([Stream])
class KafkaCollectTaskSpec extends Specification {

	static byte CONTENT_TYPE_STRING = 11
	
    def setup() {
		defineBeans {
			feedFileService(FeedFileService)
		}
    }

    def cleanup() {

    }

	void "test collecting a date range of events from Kafka"() {
		// Create Stream
		Stream stream = new Stream(name: 'KafkaCollectTaskSpec')
		stream.id = 'KafkaCollectTaskSpec'
		stream.config = ([topic:stream.id] as JSON)
		stream.save(validate:false, flush:true, failOnError:true)
		
		assert Stream.get(stream.id) != null
		
		Date beginDate = new Date()
		Date endDate = new Date()
		
		final UnifinaKafkaIterator mockedIterator = Mock()
		mockedIterator.hasNext() >>> [true, true, true, true, false]
		mockedIterator.next() >> { new UnifinaKafkaMessage("KafkaCollectTaskSpec", "KafkaCollectTaskSpec", System.currentTimeMillis(), (byte) 0, new byte[0]) }
		
		final KafkaFeedFileWriter mockedWriter = Mock()
		mockedWriter.getFile() >> Mock(File)
				
		// Create the instance to be tested
		KafkaCollectTask task = new KafkaCollectTask(new Task(), [streamId: stream.id, beginDate: beginDate.time, endDate: endDate.time, filename: "KafkaCollectTaskSpec.tmp"], grailsApplication) {
			// Return mock Kafka iterator instead of a real one
			@Override
			protected UnifinaKafkaIterator createIterator(String topic, Date from, Date to) {
				return mockedIterator
			}
			
			@Override
			protected KafkaFeedFileWriter createWriter(String name) {
				return mockedWriter
			}
		}
		
		// Mock the FeedFileService
		FeedFileService feedFileService = Mock()
		task.feedFileService = feedFileService
		
		when: "the task is run"
			boolean success = task.run()
		then: "correct number of events must be written and writer closed"
			success
			4 * mockedWriter.write(_)
			1 * mockedWriter.close()
		then: "feedFileService.createFeedFile must be called"
			1 * task.feedFileService.createFeedFile(_, _, _, _, _) //stream, beginDate, endDate, _ as File, true)
		then: "the file must be deleted"
			1 * mockedWriter.deleteFile()
	}

}
