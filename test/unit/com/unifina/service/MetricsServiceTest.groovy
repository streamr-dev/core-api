package com.unifina.service

import com.unifina.domain.security.SecUser
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(MetricsService)
class MetricsServiceTest extends Specification {

	SecUser me = new SecUser(username: "e@e.com", name: "user")

	String lastSentChannelId
	def lastSentMessage
	int valueSum
	int messageCount

	def setup() {
		service.springSecurityService = [
		    getCurrentUser: { -> me }
		]
		service.kafkaService = [
			sendMessage: { String channelId, key, message ->
				lastSentChannelId = channelId
				lastSentMessage = message
				valueSum += message.value
				messageCount++
			}
		]

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
		service.flush()
		then:
		messageCount == 2
		valueSum == sendCount * singleIncrement
		lastSentChannelId == "streamr-metrics"
		lastSentMessage.metric == "test.metric"
		lastSentMessage.user == me.id
		lastSentMessage.value == valueSum - service.reportingPeriodEvents
	}
}
