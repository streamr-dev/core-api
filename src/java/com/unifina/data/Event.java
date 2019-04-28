package com.unifina.data;

import com.streamr.client.protocol.message_layer.ITimestamped;

import java.util.Date;
import java.util.function.Consumer;

public class Event<ContentClass extends ITimestamped> implements Comparable<Event<ContentClass>> {

	private final Date timestamp;
	private final long sequenceNumber; // secondary ordering variable
	private final ContentClass content;
	private final Consumer<ContentClass> consumer;

	public Event(ContentClass content, Consumer<ContentClass> consumer) {
		this(content, content.getTimestampAsDate(), 0L, consumer);
	}

	public Event(ContentClass content, Date timestamp, Consumer<ContentClass> consumer) {
		this(content, timestamp, 0L, consumer);
	}

	public Event(ContentClass content, Date timestamp, long sequenceNumber, Consumer<ContentClass> consumer) {
		if (timestamp == null) {
			throw new IllegalArgumentException("timestamp can't be null!");
		}

		this.content = content;
		this.timestamp = timestamp;
		this.sequenceNumber = sequenceNumber;
		this.consumer = consumer;
	}

	@Override
	public int compareTo(Event<ContentClass> e) {
		int t = timestamp.compareTo(e.timestamp);
		return (t != 0) ? t : Long.compare(sequenceNumber, e.sequenceNumber);
	}

	@Override
	public String toString() {
		return timestamp + " - content: " + content;
	}

	public void dispatch() {
		if (consumer != null) {
			consumer.accept(this.getContent());
		}
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public ContentClass getContent() {
		return content;
	}
}
