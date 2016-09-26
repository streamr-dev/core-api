package com.unifina.feed

import com.unifina.data.StreamrBinaryMessage
import spock.lang.Specification

import java.nio.ByteBuffer

public class StreamrBinaryMessageSpec extends Specification {

	def "data is not altered on encode/decode"() {
		def msg = new StreamrBinaryMessage("testId", 0, System.currentTimeMillis(), StreamrBinaryMessage.CONTENT_TYPE_STRING, "foo".getBytes("UTF-8"), 100)

		when:
		byte[] encoded = msg.toBytes()
		StreamrBinaryMessage decoded = new StreamrBinaryMessage(ByteBuffer.wrap(encoded))

		then:
		decoded.getStreamId() == msg.getStreamId()
		decoded.getPartition() == msg.getPartition()
		decoded.getTimestamp() == msg.getTimestamp()
		decoded.getContentType() == msg.getContentType()
		decoded.getTTL() == msg.getTTL()
		new String(decoded.getContentBytes(), "UTF-8") == new String(msg.getContentBytes(), "UTF-8")
	}

}
