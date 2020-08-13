package com.unifina.utils

import com.streamr.client.protocol.message_layer.MessageID
import com.streamr.client.protocol.message_layer.StreamMessage
import com.streamr.client.utils.Address

public class TestUtils {
	public static StreamMessage buildMsg(String streamId, int streamPartition, Date timestamp, Map content = new HashMap()) {
		MessageID id = new MessageID(streamId, streamPartition, timestamp.getTime(), 0, new Address("publisherId"), "msgChainId")
		return new StreamMessage(id, null, content)
	}
}
