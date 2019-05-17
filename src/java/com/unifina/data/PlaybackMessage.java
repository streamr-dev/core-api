package com.unifina.data;

import java.util.Date;

class PlaybackMessage {
	public enum PlaybackEvent {
		START, END
	}
	private final PlaybackEvent event;

	static Event<PlaybackMessage> newStartEvent(Date timestamp) {
		return new Event<>(new PlaybackMessage(PlaybackEvent.START), timestamp, 0L, null);
	}

	static Event<PlaybackMessage> newEndEvent(Date timestamp) {
		return new Event<>(new PlaybackMessage(PlaybackEvent.END), timestamp, 0L, null);
	}

	private PlaybackMessage(PlaybackEvent event) {
		this.event = event;
	}

	@Override
	public String toString() {
		return "PlaybackMessage{event=" + event + "}";
	}
}
