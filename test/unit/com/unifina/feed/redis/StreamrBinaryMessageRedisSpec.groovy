package com.unifina.feed.redis

import com.unifina.data.StreamrBinaryMessage
import spock.lang.Specification

import java.nio.ByteBuffer

public class StreamrBinaryMessageRedisSpec extends Specification {

	def "data is not altered on encode/decode"() {
		def msg = new StreamrBinaryMessageRedis("testId", System.currentTimeMillis(), StreamrBinaryMessage.CONTENT_TYPE_STRING, "foo".getBytes("UTF-8"), 123, 122, 5)

		when:
		byte[] encoded = msg.toBytes()
		StreamrBinaryMessageRedis decoded = new StreamrBinaryMessageRedis(ByteBuffer.wrap(encoded))

		then:
		decoded.getStreamId() == msg.getStreamId()
		decoded.getTimestamp() == msg.getTimestamp()
		decoded.getContentType() == msg.getContentType()
		new String(decoded.getContentBytes(), "UTF-8") == new String(msg.getContentBytes(), "UTF-8")
		decoded.getOffset() == msg.getOffset()
		decoded.getPreviousOffset() == msg.getPreviousOffset()
		decoded.getPartition() == msg.getPartition()
	}

	def "null previousOffset"() {
		def msg = new StreamrBinaryMessageRedis("testId", System.currentTimeMillis(), StreamrBinaryMessage.CONTENT_TYPE_STRING, "foo".getBytes("UTF-8"), 123, null, 5)

		when:
		byte[] encoded = msg.toBytes()
		StreamrBinaryMessageRedis decoded = new StreamrBinaryMessageRedis(ByteBuffer.wrap(encoded))

		then:
		decoded.getPreviousOffset() == null
	}

}
