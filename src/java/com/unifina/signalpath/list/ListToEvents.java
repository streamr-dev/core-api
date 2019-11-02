package com.unifina.signalpath.list;

import com.unifina.data.Event;
import com.streamr.client.protocol.message_layer.ITimestamped;
import com.unifina.signalpath.*;

import java.util.Date;
import java.util.List;

/**
 * Sends out incoming List items one by one as separate events
 */
public class ListToEvents extends AbstractSignalPathModule {
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
		if (inList.size() < 1) {
			return;
		}

		// enqueue the items, send out and propagate in event handler
		for (int i = 0; i < inList.size(); i++) {
			QueuedItem queuedItem = new QueuedItem(inList.get(i), getGlobals().time);
			getGlobals().getDataSource().enqueue(new Event<>(queuedItem, queuedItem.timestamp, i, it -> {
				out.send(it.item);
				getPropagator().propagate();
			}));
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
		public Date getTimestampAsDate() {
			return timestamp;
		}
	}

	@Override
	public void clearState() {}
}
