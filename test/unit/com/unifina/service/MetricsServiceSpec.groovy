package com.unifina.service

import com.unifina.domain.security.SecUser
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(MetricsService)
class MetricsServiceSpec extends Specification {

	static SecUser me

	String lastSentChannelId
	def lastSentMessage
	int valueSum
	int messageCount

	def setupSpec() {
		me = new SecUser(username: "e@e.com", name: "user")
		me.id = 1
	}

	def setup() {
		service.springSecurityService = new SpringSecurityService() {
			@Override Object getCurrentUser() { me }
		}
		service.kafkaService = new KafkaService() {
			@Override void sendMessage(String channelId, Object key, Map message) {
				lastSentChannelId = channelId
				lastSentMessage = message
				valueSum += message.value
				messageCount++
			}
		}

		lastSentChannelId = null
		lastSentMessage = null
		valueSum = 0
		messageCount = 0
	}

	def "send out correct number of events to correct channel+user"() {
		int sendCount = 10
		when:
		(1..sendCount).each {
			service.increment("test.metric", me)
		}
		service.flush()
		then:
		messageCount == 1
		valueSum == sendCount
		lastSentChannelId == "streamr-metrics"
		lastSentMessage.metric == "test.metric"
		lastSentMessage.user == me.id
		lastSentMessage.value == sendCount
	}

	def "reportingPeriodEvents causes report to be sent"() {
		int sendCount = 10
		int singleIncrement = (int)(service.reportingPeriodEvents / sendCount) + 1
		when:
		(1..sendCount).each {
			service.increment("test.metric", me, singleIncrement)
		}
		then:
		messageCount == 1
		valueSum == sendCount * singleIncrement
		lastSentChannelId == "streamr-metrics"
		lastSentMessage.metric == "test.metric"
		lastSentMessage.user == me.id
		lastSentMessage.value == valueSum
	}

	def "disabled MetricsService won't report anything"() {
		when:
		service.increment("test.metric", me)
		service.flush()
		then:
		messageCount == 1

		when:
		service.disable()
		service.increment("test.metric", me)
		service.flush()
		then: "still one"
		messageCount == 1

		when:
		service.enable()
		service.increment("test.metric", me)
		service.flush()
		then:
		messageCount == 2
	}

	def "results are flushed before disabling"() {
		when:
		service.increment("test.metric", me)
		then:
		messageCount == 0

		when:
		service.disable()
		then:
		messageCount == 1
	}
}
