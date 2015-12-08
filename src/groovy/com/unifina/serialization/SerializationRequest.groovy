package com.unifina.serialization

import com.unifina.data.FeedEvent
import com.unifina.service.SignalPathService
import com.unifina.signalpath.SignalPath

public class SerializationRequest {

	public void serialize(SignalPath sp) {
		SignalPathService service = sp.globals.getBean(SignalPathService)
		service.saveState(sp)
	}

	public static FeedEvent makeFeedEvent(SignalPath signalPath) {
		def serializeEvent = new FeedEvent()
		serializeEvent.content = new SerializationRequest()
		serializeEvent.recipient = signalPath
		serializeEvent.timestamp = new Date()
		serializeEvent
	}
}
