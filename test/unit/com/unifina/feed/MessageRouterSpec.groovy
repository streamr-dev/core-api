package com.unifina.feed

import com.streamr.client.protocol.message_layer.StreamMessage
import com.streamr.client.utils.StreamPartition
import spock.lang.Specification

import java.util.function.Consumer


class MessageRouterSpec extends Specification {

	MessageRouter router
	StreamPartition streamPartition
	StreamMessage msg

	def setup() {
		router = new MessageRouter()
		streamPartition = new StreamPartition('streamId', 0)
		msg = Mock(StreamMessage)
		msg.getStreamId() >> 'streamId'
		msg.getStreamPartition() >> 0
	}

	def "messages are routed to correct subscribers"() {
		Consumer c1 = Mock(Consumer)
		Consumer c2 = Mock(Consumer)
		Consumer c3 = Mock(Consumer)
		router.subscribe(c1, streamPartition)
		router.subscribe(c2, streamPartition)
		router.subscribe(c3, new StreamPartition('streamId', 1))

		when:
		def result = router.route(msg)
		then:
		result == [c1, c2]
		1 * c1.accept(msg)
		1 * c2.accept(msg)
		0 * c3.accept(msg)
	}

	def "messages are not routed to unsubscribers"() {
		Consumer c1 = Mock(Consumer)
		Consumer c2 = Mock(Consumer)
		router.subscribe(c1, streamPartition)
		router.subscribe(c2, streamPartition)
		router.unsubscribe(c2, streamPartition)

		when:
		def result = router.route(msg)
		then:
		result == [c1]
		1 * c1.accept(msg)
		0 * c2.accept(msg)
	}

}
