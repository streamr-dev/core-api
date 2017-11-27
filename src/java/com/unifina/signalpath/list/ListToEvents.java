package com.unifina.signalpath.list;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;
import com.unifina.feed.ITimestamped;
import com.unifina.signalpath.*;

import java.util.Date;
import java.util.List;

/**
 * Sends out incoming List items one by one as separate events
 */
public class ListToEvents extends AbstractSignalPathModule implements IEventRecipient {
	private final ListInput in = new ListInput(this, "list");
	private final Output<Object> out = new Output<>(this, "item", "Object");

	@Override
	public void init() {
		super.init();
		setPropagationSink(true);
	}

	@Override
	public void sendOutput() {
		List inList = in.getValue();
		if (inList.size() < 1) { return; }

		// enqueue the items, send out in receive() below
		for (Object item : inList) {
			QueuedItem queuedItem = new QueuedItem(item, getGlobals().time);
			getGlobals().getDataSource().getEventQueue().enqueue(new FeedEvent<>(queuedItem, queuedItem.timestamp, this));
		}
	}

	@Override
	public void receive(FeedEvent event) {
		if (event.content instanceof QueuedItem) {
			out.send(((QueuedItem)event.content).item);
			getPropagator().propagate();
		} else {
			super.receive(event);
		}
	}

	private transient Propagator listItemPropagator;
	private Propagator getPropagator() {
		if (listItemPropagator == null) {
			listItemPropagator = new Propagator(this);
		}
		return listItemPropagator;
	}

	public static class QueuedItem implements ITimestamped {
		public Object item;
		public Date timestamp;

		public QueuedItem(Object item, Date timestamp) {
			this.item = item;
			this.timestamp = timestamp;
		}

		@Override
		public Date getTimestamp() {
			return timestamp;
		}
	}

	@Override
	public void clearState() {}
}
