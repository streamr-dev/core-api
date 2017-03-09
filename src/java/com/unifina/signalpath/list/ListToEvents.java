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
	public void sendOutput() {
		List inList = in.getValue();
		if (inList.size() < 1) { return; }

		// send out first item immediately
		out.send(inList.get(0));

		// enqueue the rest, send out in receive() below
		for (int i = 1; i < inList.size(); i++) {
			Packet packet = new Packet();
			packet.payload = inList.get(i);
			packet.timestamp = getGlobals().isRealtime() ? new Date() : getGlobals().time;
			getGlobals().getDataSource().getEventQueue().enqueue(new FeedEvent<>(packet, packet.timestamp, this));
		}
	}

	@Override
	public void receive(FeedEvent event) {
		if (event.content instanceof Packet) {
			out.send(((Packet)event.content).payload);
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

	private class Packet implements ITimestamped {
		public Object payload;
		public Date timestamp;

		@Override
		public Date getTimestamp() {
			return timestamp;
		}
	}

	@Override
	public void clearState() {}
}
