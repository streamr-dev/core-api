package com.unifina.service

import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(ControllerUnitTestMixin) // JSON support
@TestFor(StreamService)
@Mock([Stream, Feed, FeedFile, SecUser])
class StreamServiceSpec extends Specification {

	def kafkaService
	
	def setup() {
		kafkaService = Mock(KafkaService)
		service.kafkaService = kafkaService
	}

	void "createUserStream must not call kafkaService.createTopics() if input incomplete"() {
		
		when:
		service.createUserStream([:], null)

		then:
		0 * kafkaService.createTopics(_)
	}

	void "createUserStream results in persisted Stream"() {
		when:
		service.createUserStream([name: "name", localId: "localId"], null)

		then:
		Stream.count() == 1
		Stream.list().first().name == "name"
		Stream.list().first().localId == "localId"
	}

	void "createUserStream calls kafkaService.createTopics"() {
		Stream stream

		when:
		stream = service.createUserStream([name: "name", localId: "localId"], null)

		then:
		1 * kafkaService.createTopics(_)
	}
	

	void "getDataRange gives correct values"() {
		Map dataRange
		Stream stream

		setup:
		SecUser user = new SecUser(id: 1, username: "user@user.com", password: "pwd", name:"name", enabled:true, timezone: "Europe/Helsinki")
		user.save()
		stream = service.createUserStream([name: "streamName", localId:1], user)
		FeedFile startFile = new FeedFile(name:"start", feed:new Feed(), stream: stream, day: new Date(1440000000000), beginDate: new Date(1440000000000), endDate: new Date(1440000000000))
		FeedFile endFile = new FeedFile(name:"end", feed:new Feed(), stream: stream, day: new Date(1450000000000), beginDate: new Date(1450000000000), endDate: new Date(1450000000000))
		startFile.save()
		endFile.save()

		when: "asked for the dataRange"
		dataRange = service.getDataRange(stream)

		then: "the dates are correct"
		dataRange != null
		dataRange.beginDate == new Date(1440000000000)
		dataRange.endDate == new Date(1450000000000)
	}

	void "getDataRange gives empty values if there are no feedFiles"(){
		Map dataRange
		Stream stream

		setup:
		SecUser user = new SecUser(id: 1, username: "user@user.com", password: "pwd", name:"name", enabled:true, timezone: "Europe/Helsinki")
		user.save()
		stream = service.createUserStream([name: "streamName", localId:1], user)

		when: "asked for the dataRange"
		dataRange = service.getDataRange(stream)

		then: "the dates are correct"
		dataRange.beginDate == null
		dataRange.endDate == null
	}
}
