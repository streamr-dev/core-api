package com.unifina.feed

import com.unifina.data.StreamrBinaryMessage
import com.unifina.data.StreamrBinaryMessageV28
import com.unifina.data.StreamrBinaryMessageV29
import spock.lang.Specification

import java.nio.ByteBuffer

class StreamrBinaryMessageSpec extends Specification {

	StreamrBinaryMessage msg = new StreamrBinaryMessageV28("testId", 0, System.currentTimeMillis(), 100,
		StreamrBinaryMessageV28.CONTENT_TYPE_STRING, "foobar hello world 666".getBytes("UTF-8"))
	StreamrBinaryMessage msg2 = new StreamrBinaryMessageV29("testId", 0, System.currentTimeMillis(), 100,
		StreamrBinaryMessageV28.CONTENT_TYPE_STRING, "foobar hello world 666".getBytes("UTF-8"),
		StreamrBinaryMessageV29.SignatureType.SIGNATURE_TYPE_ETH, '0xF915eD664e43C50eB7b9Ca7CfEB992703eDe55c4',
		'0xcb1fa20f2f8e75f27d3f171d236c071f0de39e4b497c51b390306fc6e7e112bb415ecea1bd093320dd91fd91113748286711122548c52a15179822a014dc14931b')

	def "data is not altered on encode/decode"() {

		when:
		byte[] encoded = msg.toBytes()
		StreamrBinaryMessage decoded = StreamrBinaryMessage.from(ByteBuffer.wrap(encoded))

		then:
		decoded.getStreamId() == msg.getStreamId()
		decoded.getPartition() == msg.getPartition()
		decoded.getTimestamp() == msg.getTimestamp()
		decoded.getContentType() == msg.getContentType()
		decoded.getTTL() == msg.getTTL()
		new String(decoded.getContentBytes(), "UTF-8") == new String(msg.getContentBytes(), "UTF-8")
	}

	def "data is not altered on encode/decode for version 29"() {

		when:
		byte[] encoded = msg2.toBytes()
		StreamrBinaryMessage decoded = StreamrBinaryMessage.from(ByteBuffer.wrap(encoded))

		then:
		decoded.getStreamId() == msg2.getStreamId()
		decoded.getPartition() == msg2.getPartition()
		decoded.getTimestamp() == msg2.getTimestamp()
		decoded.getContentType() == msg2.getContentType()
		decoded.getTTL() == msg2.getTTL()
		new String(decoded.getContentBytes(), "UTF-8") == new String(msg2.getContentBytes(), "UTF-8")
	}

	def "sizeInBytes reports correct size"() {
		expect:
		msg.toBytes().length == msg.sizeInBytes()
		msg2.toBytes().length == msg2.sizeInBytes()
	}

}
