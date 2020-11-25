package com.unifina.data;

import java.util.Date;
import java.util.function.Consumer;

/**
 * An Event is a wrapper class used for async dispatch of content to an attached Consumer.
 * Event have a natural ordering based on the (timestamp, sequenceNumber) pair.
 * @param <ContentClass> This is the type of the content, which is arbitrary. The generic is there to ensure that the content and Consumer are compatible.
 */
public class Event<ContentClass> implements Comparable<Event<ContentClass>> {

	private final Date timestamp;
	private final long sequenceNumber; // secondary ordering variable
	private final ContentClass content;
	private final Consumer<ContentClass> consumer;

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
		return "Event[timestamp=" + timestamp + ",content=" + content + ']';
	}

	public void dispatch() {
		System.out.println("DEBUG Event dispatch.1 content=" + this.getContent());
		// In special cases (such as when ticking the clock) the consumer may be null, so check
		if (consumer != null) {
			consumer.accept(this.getContent());
		}
		System.out.println("DEBUG Event dispatch.2");
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public ContentClass getContent() {
		return content;
	}
}
