package com.unifina.signalpath.utils;

import com.streamr.client.protocol.message_layer.MessageID;
import com.streamr.client.protocol.message_layer.MessageRef;
import com.streamr.client.protocol.message_layer.StreamMessage;
import com.streamr.client.protocol.message_layer.StreamMessageV30;
import com.unifina.data.StreamPartitioner;
import com.unifina.domain.data.Stream;
import com.unifina.domain.security.SecUser;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MessageChainUtil implements Serializable {
	private Map<String,Long> previousTimestamps = new HashMap<>();
	private Map<String,Long> previousSequenceNumbers = new HashMap<>();

	private transient SecUser cachedUser = null;

	private static final Logger log = Logger.getLogger(MessageChainUtil.class);

	private SecUser getUser(Long userId) {
		if (cachedUser == null) {
			cachedUser = SecUser.getViaJava(userId);
		}
		return cachedUser;
	}

	private long getNextSequenceNumber(String key, long timestamp) {
		if (!previousTimestamps.containsKey(key) || previousTimestamps.get(key) != timestamp) {
			return 0L;
		}
		if (!previousSequenceNumbers.containsKey(key)) {
			previousSequenceNumbers.put(key, 0L);
		}
		return previousSequenceNumbers.get(key)+1;
	}

	private MessageRef getPreviousMessageRef(String key) {
		if (!previousTimestamps.containsKey(key)) {
			return null;
		}
		if (!previousSequenceNumbers.containsKey(key)) {
			previousSequenceNumbers.put(key, 0L);
		}
		return new MessageRef(previousTimestamps.get(key), previousSequenceNumbers.get(key));
	}

	public StreamMessage getStreamMessage(Stream stream, Date timestampAsDate, Map content, Long userId){
		int streamPartition = StreamPartitioner.partition(stream, null);
		String key = stream.getId()+streamPartition;
		long timestamp = timestampAsDate.getTime();
		long sequenceNumber = getNextSequenceNumber(key, timestamp);
		SecUser user = getUser(userId);
		String publisherId = user.getPublisherId();
		MessageID msgId = new MessageID(stream.getId(), streamPartition, timestamp, sequenceNumber, publisherId);
		MessageRef prevMsgRef = this.getPreviousMessageRef(key);
		try {
			StreamMessage msg = new StreamMessageV30(msgId, prevMsgRef, StreamMessage.ContentType.CONTENT_TYPE_JSON,
					content, StreamMessage.SignatureType.SIGNATURE_TYPE_NONE, null);
			previousTimestamps.put(key, timestamp);
			previousSequenceNumbers.put(key, sequenceNumber);
			return msg;
		} catch (IOException e) {
			log.error(e);
		}
		return null;
	}
}
