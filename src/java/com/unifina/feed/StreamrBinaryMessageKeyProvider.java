package com.unifina.feed;

import com.unifina.data.IStreamRequirement;
import com.unifina.domain.data.Feed;
import com.unifina.utils.Globals;

public class StreamrBinaryMessageKeyProvider extends AbstractKeyProvider<IStreamRequirement, StreamrMessage, String> {

	public StreamrBinaryMessageKeyProvider(Globals globals, Feed feed) {
		super(globals, feed);
	}

	@Override
	public String getSubscriberKey(IStreamRequirement subscriber) {
		return subscriber.getStream().getId();
	}

	@Override
	public String getMessageKey(StreamrMessage message) {
		return message.streamId;
	}

}
