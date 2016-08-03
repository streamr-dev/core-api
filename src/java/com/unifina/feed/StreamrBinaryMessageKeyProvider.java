package com.unifina.feed;

import com.unifina.data.IStreamRequirement;
import com.unifina.data.StreamrBinaryMessage;
import com.unifina.domain.data.Feed;
import com.unifina.utils.Globals;

public class StreamrBinaryMessageKeyProvider extends AbstractKeyProvider<IStreamRequirement, StreamrBinaryMessage, String> {

	public StreamrBinaryMessageKeyProvider(Globals globals, Feed feed) {
		super(globals, feed);
	}

	@Override
	public String getSubscriberKey(IStreamRequirement subscriber) {
		return subscriber.getStream().getId();
	}

	@Override
	public String getMessageKey(StreamrBinaryMessage message) {
		return message.getStreamId();
	}

}
