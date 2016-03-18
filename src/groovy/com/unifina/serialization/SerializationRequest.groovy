package com.unifina.serialization

import com.unifina.data.FeedEvent
import com.unifina.feed.ITimestamped
import com.unifina.service.SignalPathService
import com.unifina.signalpath.SignalPath

public class SerializationRequest implements ITimestamped {

	Date timestamp

	public SerializationRequest(Date timestamp) {
		this.timestamp = timestamp
	}

	public void serialize(SignalPath sp) {
		SignalPathService service = sp.globals.getBean(SignalPathService)
		service.saveState(sp)
	}

	public static FeedEvent makeFeedEvent(SignalPath signalPath) {
		Date timestamp = new Date()
		return new FeedEvent(new SerializationRequest(timestamp), timestamp, signalPath)
	}

	@Override
	Date getTimestamp() {
		return timestamp
	}
}
