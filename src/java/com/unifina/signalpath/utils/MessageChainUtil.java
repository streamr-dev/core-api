package com.unifina.signalpath.utils;

import com.streamr.client.protocol.message_layer.MessageID;
import com.streamr.client.protocol.message_layer.MessageRef;
import com.streamr.client.protocol.message_layer.StreamMessage;
import com.streamr.client.protocol.message_layer.StreamMessageV31;
import com.unifina.data.StreamPartitioner;
import com.unifina.domain.data.Stream;
import com.unifina.domain.security.SecUser;
import com.unifina.utils.IdGenerator;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/**
 * Generates StreamMessage chains by maintaining a state of the last message reference per
 * streamId-streamPartition-publisherId-msgChainId.
 */
public class MessageChainUtil implements Serializable {
	private Map<String,Long> previousTimestamps = new HashMap<>();
	private Map<String,Long> previousSequenceNumbers = new HashMap<>();

	private Long userId;
	private String msgChainId;
	private transient SecUser user;

	public MessageChainUtil(Long userId) {
		this.userId = userId;
		this.msgChainId = IdGenerator.getShort();
	}

	private SecUser getUser() {
		if (user == null) {
			user = SecUser.getViaJava(userId);
		}
		return user;
	}

	private long getNextSequenceNumber(String key, long timestamp) {
		Long previousTimestamp = previousTimestamps.get(key);
		if (previousTimestamp == null || previousTimestamp != timestamp) {
			return 0L;
		}
		Long previousSequenceNumber = previousSequenceNumbers.get(key);
		return previousSequenceNumber == null ? 0L : previousSequenceNumber + 1;
	}

	private MessageRef getPreviousMessageRef(String key) {
		Long previousTimestamp = previousTimestamps.get(key);
		Long previousSequenceNumber = previousSequenceNumbers.get(key);
		if (previousTimestamp == null || previousSequenceNumber == null) {
			return null;
		}
		return new MessageRef(previousTimestamp, previousSequenceNumber);
	}

	public StreamMessage getStreamMessage(Stream stream, Date timestampAsDate, Map content){
		int streamPartition = StreamPartitioner.partition(stream, null);
		String key = stream.getId()+streamPartition;
		long timestamp = timestampAsDate.getTime();
		long sequenceNumber = getNextSequenceNumber(key, timestamp);
		String publisherId = getUser().getPublisherId();
		MessageID msgId = new MessageID(stream.getId(), streamPartition, timestamp, sequenceNumber, publisherId, msgChainId);
		MessageRef prevMsgRef = this.getPreviousMessageRef(key);
		StreamMessage msg = new StreamMessageV31(msgId, prevMsgRef, StreamMessage.ContentType.CONTENT_TYPE_JSON, StreamMessage.EncryptionType.NONE,
				content, StreamMessage.SignatureType.SIGNATURE_TYPE_NONE, null);
		previousTimestamps.put(key, timestamp);
		previousSequenceNumbers.put(key, sequenceNumber);
		return msg;
	}
}
