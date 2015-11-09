package com.unifina.serialization

import com.unifina.signalpath.SignalPath

public class SerializationRequest {

	public void serialize(SignalPath sp) {
		def rsp = sp.runningSignalPath
		rsp.serialized = sp.globals.serializationService.serialize(sp)
		rsp.save()
	}
}
