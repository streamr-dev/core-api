package com.unifina.serialization

import com.unifina.data.FeedEvent
import com.unifina.feed.ITimestamped
import com.unifina.service.SignalPathService
import com.unifina.signalpath.SignalPath

class SerializationRequest implements ITimestamped {

	Date timestamp

	SerializationRequest(Date timestamp) {
		this.timestamp = timestamp
	}

	void serialize(SignalPath sp) {
		SignalPathService service = sp.globals.getBean(SignalPathService)
		service.saveState(sp)
	}

	static FeedEvent makeFeedEvent(SignalPath signalPath) {
		Date timestamp = new Date()
		return new FeedEvent(new SerializationRequest(timestamp), timestamp, signalPath)
	}

	@Override
	Date getTimestamp() {
		return timestamp
	}
}
