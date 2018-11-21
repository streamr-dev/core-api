package com.unifina.feed.redis

import com.unifina.data.StreamrBinaryMessage
import spock.lang.Specification

import java.nio.ByteBuffer

public class StreamrBinaryMessageWithKafkaMetadataSpec extends Specification {

	def "data is not altered on encode/decode"() {
		def msg = new StreamrBinaryMessageWithKafkaMetadata("testId", 0, System.currentTimeMillis(), 5, StreamrBinaryMessage.CONTENT_TYPE_STRING, "foo".getBytes("UTF-8"), 0, 123, 122)
		StreamrBinaryMessage msgPayload = msg.getStreamrBinaryMessage()
		when:
		byte[] encoded = msg.toBytesWithKafkaMetadata()
		StreamrBinaryMessageWithKafkaMetadata decoded = new StreamrBinaryMessageWithKafkaMetadata(ByteBuffer.wrap(encoded))
		StreamrBinaryMessage decodedPayload = decoded.getStreamrBinaryMessage()

		then:
		decodedPayload.getStreamId() == msgPayload.getStreamId()
		decodedPayload.getTimestamp() == msgPayload.getTimestamp()
		decodedPayload.getContentType() == msgPayload.getContentType()
		new String(decodedPayload.getContentBytes(), "UTF-8") == new String(msgPayload.getContentBytes(), "UTF-8")
		decoded.getOffset() == msg.getOffset()
		decoded.getPreviousOffset() == msg.getPreviousOffset()
		decodedPayload.getPartition() == msgPayload.getPartition()
	}

	def "null previousOffset"() {
		def msg = new StreamrBinaryMessageWithKafkaMetadata("testId", 0, System.currentTimeMillis(), 5, StreamrBinaryMessage.CONTENT_TYPE_STRING, "foo".getBytes("UTF-8"), 0, 123, null)

		when:
		byte[] encoded = msg.toBytesWithKafkaMetadata()
		StreamrBinaryMessageWithKafkaMetadata decoded = new StreamrBinaryMessageWithKafkaMetadata(ByteBuffer.wrap(encoded))

		then:
		decoded.getPreviousOffset() == null
	}

}
