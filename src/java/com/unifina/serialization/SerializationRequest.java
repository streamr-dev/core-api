package com.unifina.serialization;

import com.unifina.service.SerializationService;
import com.unifina.signalpath.SignalPath;

public class SerializationRequest {

	private SerializationService serializationService = new SerializationService();

	public void serialize(SignalPath signalPath) {
		serializationService.serialize(signalPath, "signalpath.json");
		serializationService.deserialize("signalpath.json");
	}
}
