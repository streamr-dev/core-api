package com.unifina.serialization


import com.streamr.client.protocol.message_layer.ITimestamped
import com.unifina.data.Event;
import com.unifina.service.SignalPathService
import com.unifina.signalpath.SignalPath
import grails.util.Holders

class SerializationRequest implements ITimestamped {

	Date timestamp

	SerializationRequest(Date timestamp) {
		this.timestamp = timestamp
	}

	static Event makeFeedEvent(final SignalPath signalPath) {
		Date timestamp = new Date()
		return new Event(new SerializationRequest(timestamp), timestamp, 0L, () -> {
			SignalPathService service = Holders.getApplicationContext().getBean(SignalPathService)
			service.saveState(signalPath)
		})
	}

	@Override
	Date getTimestampAsDate() {
		return timestamp
	}
}
