package com.unifina.data;

import com.unifina.feed.ITimestamped;

import java.util.Date;

class PlaceholderFeedEvent extends FeedEvent<ITimestamped, IEventRecipient> {
	PlaceholderFeedEvent(Date timestamp) {
		super(null, new Date(timestamp.getTime()), null);
	}

	@Override
	void deliverToRecipient() {}
}
