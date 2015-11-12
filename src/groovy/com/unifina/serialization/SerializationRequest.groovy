package com.unifina.serialization

import com.unifina.data.FeedEvent
import com.unifina.domain.signalpath.RunningSignalPath
import com.unifina.signalpath.SignalPath
import org.apache.log4j.Logger

public class SerializationRequest {

	private static final Logger log = Logger.getLogger(SerializationRequest.class);

	public void serialize(SignalPath sp) {
		def rsp = sp.runningSignalPath
		rsp.serialized = sp.globals.serializationService.serialize(sp)
		RunningSignalPath.withTransaction { rsp.save() }
		log.info("RunningSignalPath " + rsp.id + " serialized")
	}

	public static FeedEvent makeFeedEvent(SignalPath signalPath) {
		def serializeEvent = new FeedEvent()
		serializeEvent.content = new SerializationRequest()
		serializeEvent.recipient = signalPath
		serializeEvent.timestamp = new Date()
		serializeEvent
	}
}
