package com.unifina.signalpath.utils

import com.streamr.client.protocol.message_layer.StreamMessage
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock([SecUser])
class MessageChainUtilSpec extends Specification {

	MessageChainUtil msgChainUtil
	Map content = [foo: "bar"]
	Stream stream = new Stream()
	String hashedUsername
	def setup() {
		stream.id = "streamId"
		Long userId = 1
		SecUser user = new SecUser(id: userId, username: 'user').save(failOnError: true, validate: false)
		msgChainUtil = new MessageChainUtil(userId)
		hashedUsername = user.getPublisherId()
	}

	void "construct with publisher id"() {
		setup:
		Date date = new Date()
		String address = "0xacfcc76b85614c416468cbabc7990afd9ab8d1a1"
		msgChainUtil = new MessageChainUtil(address)
		hashedUsername = address
		when:
		StreamMessage msg1 = msgChainUtil.getStreamMessage(stream, date, content)
		StreamMessage msg2 = msgChainUtil.getStreamMessage(stream, date, content)
		StreamMessage msg3 = msgChainUtil.getStreamMessage(stream, date, content)
		then:
		msg1.getStreamId() == stream.getId()
		msg1.getPublisherId().toString() == hashedUsername
		msg1.getTimestamp() == date.getTime()
		msg1.getSequenceNumber() == 0
		msg1.getPreviousMessageRef() == null
		msg1.getParsedContent() == content
		msg2.getStreamId() == stream.getId()
		msg2.getPublisherId().toString() == hashedUsername
		msg2.getTimestamp() == date.getTime()
		msg2.getSequenceNumber() == 1
		msg2.getPreviousMessageRef().timestamp == date.getTime()
		msg2.getPreviousMessageRef().sequenceNumber == 0
		msg3.getStreamId() == stream.getId()
		msg3.getPublisherId().toString() == hashedUsername
		msg3.getTimestamp() == date.getTime()
		msg3.getSequenceNumber() == 2
		msg3.getPreviousMessageRef().timestamp == date.getTime()
		msg3.getPreviousMessageRef().sequenceNumber == 1
	}

	void "chains correctly messages with same timestamp"() {
		Date date = new Date()
		when:
		StreamMessage msg1 = msgChainUtil.getStreamMessage(stream, date, content)
		StreamMessage msg2 = msgChainUtil.getStreamMessage(stream, date, content)
		StreamMessage msg3 = msgChainUtil.getStreamMessage(stream, date, content)
		then:
		msg1.getStreamId() == stream.getId()
		msg1.getPublisherId().toString() == hashedUsername
		msg1.getTimestamp() == date.getTime()
		msg1.getSequenceNumber() == 0
		msg1.getPreviousMessageRef() == null
		msg1.getParsedContent() == content
		msg2.getStreamId() == stream.getId()
		msg2.getPublisherId().toString() == hashedUsername
		msg2.getTimestamp() == date.getTime()
		msg2.getSequenceNumber() == 1
		msg2.getPreviousMessageRef().timestamp == date.getTime()
		msg2.getPreviousMessageRef().sequenceNumber == 0
		msg3.getStreamId() == stream.getId()
		msg3.getPublisherId().toString() == hashedUsername
		msg3.getTimestamp() == date.getTime()
		msg3.getSequenceNumber() == 2
		msg3.getPreviousMessageRef().timestamp == date.getTime()
		msg3.getPreviousMessageRef().sequenceNumber == 1
	}

	void "chains correctly messages with different timestamps"() {
		Date date1 = new Date()
		Date date2 = new Date(date1.getTime()+1000)
		Date date3 = new Date(date2.getTime()+1000)
		when:
		StreamMessage msg1 = msgChainUtil.getStreamMessage(stream, date1, content)
		StreamMessage msg2 = msgChainUtil.getStreamMessage(stream, date2, content)
		StreamMessage msg3 = msgChainUtil.getStreamMessage(stream, date3, content)
		then:
		msg1.getStreamId() == stream.getId()
		msg1.getPublisherId().toString() == hashedUsername
		msg1.getTimestamp() == date1.getTime()
		msg1.getSequenceNumber() == 0
		msg1.getPreviousMessageRef() == null
		msg1.getParsedContent() == content
		msg2.getStreamId() == stream.getId()
		msg2.getPublisherId().toString() == hashedUsername
		msg2.getTimestamp() == date2.getTime()
		msg2.getSequenceNumber() == 0
		msg2.getPreviousMessageRef().timestamp == date1.getTime()
		msg2.getPreviousMessageRef().sequenceNumber == 0
		msg3.getStreamId() == stream.getId()
		msg3.getPublisherId().toString() == hashedUsername
		msg3.getTimestamp() == date3.getTime()
		msg3.getSequenceNumber() == 0
		msg3.getPreviousMessageRef().timestamp == date2.getTime()
		msg3.getPreviousMessageRef().sequenceNumber == 0
	}

	void "chains messages separately on different streams"() {
		Date date1 = new Date()
		Date date2 = new Date(date1.getTime()+1000)
		Date date3 = new Date(date2.getTime()+1000)
		Stream stream2 = new Stream()
		stream2.id = "streamId2"
		when:
		StreamMessage msg1 = msgChainUtil.getStreamMessage(stream, date1, content)
		StreamMessage msg2 = msgChainUtil.getStreamMessage(stream2, date2, content)
		StreamMessage msg3 = msgChainUtil.getStreamMessage(stream, date3, content)
		StreamMessage msg4 = msgChainUtil.getStreamMessage(stream2, date2, content)
		then:
		msg1.getStreamId() == stream.getId()
		msg1.getPublisherId().toString() == hashedUsername
		msg1.getTimestamp() == date1.getTime()
		msg1.getSequenceNumber() == 0
		msg1.getPreviousMessageRef() == null
		msg1.getParsedContent() == content
		msg2.getStreamId() == stream2.getId()
		msg2.getPublisherId().toString() == hashedUsername
		msg2.getTimestamp() == date2.getTime()
		msg2.getSequenceNumber() == 0
		msg2.getPreviousMessageRef() == null
		msg3.getStreamId() == stream.getId()
		msg3.getPublisherId().toString() == hashedUsername
		msg3.getTimestamp() == date3.getTime()
		msg3.getSequenceNumber() == 0
		msg3.getPreviousMessageRef().timestamp == date1.getTime()
		msg3.getPreviousMessageRef().sequenceNumber == 0
		msg4.getStreamId() == stream2.getId()
		msg4.getPublisherId().toString() == hashedUsername
		msg4.getTimestamp() == date2.getTime()
		msg4.getSequenceNumber() == 1
		msg4.getPreviousMessageRef().timestamp == date2.getTime()
		msg4.getPreviousMessageRef().sequenceNumber == 0
	}
}
