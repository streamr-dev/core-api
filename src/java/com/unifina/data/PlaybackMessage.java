package com.unifina.data;

import com.unifina.feed.ITimestamped;

import java.util.Date;

class PlaybackMessage implements ITimestamped {
	private final String code;
	private final Date timestamp;

	static FeedEvent<PlaybackMessage, IEventRecipient> newStartEvent(Date timestamp) {
		return new FeedEvent<>(new PlaybackMessage("start", timestamp), timestamp, null);
	}

	static FeedEvent<PlaybackMessage, IEventRecipient> newEndEvent(Date timestamp) {
		return new FeedEvent<>(new PlaybackMessage("end", timestamp), timestamp, null);
	}

	private PlaybackMessage(String code, Date timestamp) {
		this.code = code;
		this.timestamp = timestamp;
	}

	@Override
	public Date getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString() {
		return "PlaybackMessage{" +
			"code='" + code + '\'' +
			", timestamp=" + timestamp +
			'}';
	}
}
