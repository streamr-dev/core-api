package com.unifina.feed;

import com.streamr.client.protocol.message_layer.StreamMessage;
import com.unifina.data.IStreamRequirement;
import com.unifina.domain.data.Feed;
import com.unifina.utils.Globals;

import java.util.ArrayList;
import java.util.List;

public class StreamrBinaryMessageKeyProvider extends AbstractKeyProvider<IStreamRequirement, StreamMessage, String> {

	public StreamrBinaryMessageKeyProvider(Globals globals, Feed feed) {
		super(globals, feed);
	}

	@Override
	public List<String> getSubscriberKeys(IStreamRequirement subscriber) {
		List<String> keys = new ArrayList<>(subscriber.getPartitions().size());
		for (Integer partition : subscriber.getPartitions()) {
			keys.add(subscriber.getStream().getId() + "-" + partition);
		}
		return keys;
	}

	@Override
	public String getMessageKey(StreamMessage message) {
		return message.getStreamId() + "-" + message.getStreamPartition();
	}

}
