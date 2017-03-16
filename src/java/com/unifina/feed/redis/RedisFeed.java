package com.unifina.feed.redis;

import com.unifina.domain.data.Feed;
import com.unifina.feed.AbstractFeedProxy;
import com.unifina.feed.AbstractStreamrMessage;
import com.unifina.feed.StreamrMessageEventRecipient;
import com.unifina.signalpath.utils.ConfigurableStreamModule;
import com.unifina.utils.Globals;

public class RedisFeed extends AbstractFeedProxy<ConfigurableStreamModule, StreamrBinaryMessageWithKafkaMetadata, AbstractStreamrMessage, String, StreamrMessageEventRecipient> {

	public RedisFeed(Globals globals, Feed domainObject) {
		super(globals, domainObject);
	}

}
