package com.unifina.feed.redis

import com.unifina.data.StreamrBinaryMessage
import spock.lang.Specification

import java.nio.ByteBuffer

public class StreamrBinaryMessageFromRedisSpec extends Specification {

	def "data is not altered on encode/decode"() {
		def msg = new StreamrBinaryMessageFromRedis("testId", System.currentTimeMillis(), StreamrBinaryMessage.CONTENT_TYPE_STRING, "foo".getBytes("UTF-8"), 123, 5)

		when:
		byte[] encoded = msg.toBytes()
		StreamrBinaryMessageFromRedis decoded = new StreamrBinaryMessageFromRedis(ByteBuffer.wrap(encoded))

		then:
		decoded.getStreamId() == msg.getStreamId()
		decoded.getTimestamp() == msg.getTimestamp()
		decoded.getContentType() == msg.getContentType()
		new String(decoded.getContentBytes(), "UTF-8") == new String(msg.getContentBytes(), "UTF-8")
		decoded.getOffset() == msg.getOffset()
		decoded.getPartition() == msg.getPartition()
	}

}
