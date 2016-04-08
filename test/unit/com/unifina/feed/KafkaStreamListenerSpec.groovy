package com.unifina.feed

import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.feed.kafka.KafkaStreamListener
import com.unifina.service.FeedFileService
import com.unifina.service.KafkaService
import grails.test.mixin.Mock
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.springframework.context.ApplicationContext
import spock.lang.Specification

@Mock([FeedFile, Stream])
class KafkaStreamListenerSpec extends Specification {

	KafkaService kafkaService = Mock(KafkaService)
	FeedFileService feedFileService = Mock(FeedFileService)
	AbstractStreamListener listener

	void setup() {

		// Necessary hack
		FeedFile.metaClass.static.executeUpdate = { String query, Map args -> return true }

		// Setup application context
		def applicationContext = Stub(ApplicationContext) {
			getBean(KafkaService) >> kafkaService
			getBean(FeedFileService) >> feedFileService
		}

		// Setup grailsApplication
		def grailsApplication = new DefaultGrailsApplication()
		grailsApplication.setMainContext(applicationContext)

		listener = new KafkaStreamListener(grailsApplication)

	}

	void "addToConfiguration() grabs topic form Stream and adds as entry"() {
		def config = [a: "a"]
		Stream stream = new Stream()
		stream.id = "uuid"

		when:
		listener.addToConfiguration(config, stream)

		then:
		config == [a: "a", topic: "uuid"]
		0 * kafkaService._
		0 * feedFileService._
	}

	void "afterStreamSaved() invokes kafkaService for topic creation"() {
		Stream stream = new Stream()
		stream.id = "uuid"

		when:
		listener.afterStreamSaved(stream)

		then:
		1 * kafkaService.createTopics(["uuid"])
		0 * kafkaService._
		0 * feedFileService._
	}

	void "afterStreamSaved() does nothing if Stream has errors"() {
		Stream stream = new Stream()
		stream.id = "uuid"
		stream.validate()

		when:
		listener.afterStreamSaved(stream)

		then:
		0 * kafkaService._
		0 * feedFileService._
	}

	void "beforeDelete() invokes kafkaService for topic deletion"() {
		Stream stream = new Stream()
		stream.id = "uuid"

		when:
		listener.beforeDelete(stream)

		then:
		1 * kafkaService.deleteTopics(["uuid"])
		0 * kafkaService._
	}

	void "beforeDelete() invokes feedFileService to delete associated FeedFiles"() {
		Stream stream = new Stream()
		stream.save(validate: false)

		def feedFiles = (1..3).collect() {
			FeedFile ff = new FeedFile(name: "ff-$it", stream: stream)
			ff.save(validate: false)
		}

		when:
		listener.beforeDelete(stream)

		then:
		1 * feedFileService.deleteFile(feedFiles[0])
		1 * feedFileService.deleteFile(feedFiles[1])
		1 * feedFileService.deleteFile(feedFiles[2])
		0 * feedFileService._
	}
}
