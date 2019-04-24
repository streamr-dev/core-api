package com.unifina.data;

import com.streamr.client.protocol.message_layer.ITimestamped;

import java.util.Date;

class PlaybackMessage implements ITimestamped {
	private final String code;
	private final Date timestamp;

	static Event<PlaybackMessage> newStartEvent(Date timestamp) {
		return new Event<>(new PlaybackMessage("start", timestamp), timestamp, 0L, null);
	}

	static Event<PlaybackMessage> newEndEvent(Date timestamp) {
		return new Event<>(new PlaybackMessage("end", timestamp), timestamp, 0L, null);
	}

	private PlaybackMessage(String code, Date timestamp) {
		this.code = code;
		this.timestamp = timestamp;
	}

	@Override
	public Date getTimestampAsDate() {
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
