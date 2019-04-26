package com.unifina.data;

import com.streamr.client.protocol.message_layer.ITimestamped;
import com.unifina.feed.FeedEventIterator;

import java.util.Date;
import java.util.function.Consumer;

public class HistoricalEvent<ContentClass extends ITimestamped> extends Event<ContentClass> {

	private final FeedEventIterator<ContentClass> iterator;

	public HistoricalEvent(ContentClass content, Date timestamp, long sequenceNumber,  Consumer<ContentClass> consumer, FeedEventIterator<ContentClass> iterator) {
		super(content, timestamp, sequenceNumber, consumer);
		this.iterator = iterator;
	}

	public FeedEventIterator<ContentClass> getIterator() {
		return iterator;
	}

}
