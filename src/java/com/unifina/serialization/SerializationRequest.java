package com.unifina.serialization;


import com.streamr.client.protocol.message_layer.ITimestamped;
import com.unifina.data.Event;
import com.unifina.service.SignalPathService;
import com.unifina.signalpath.SignalPath;
import grails.util.Holders;

import java.util.Date;

public class SerializationRequest implements ITimestamped {

	private Date timestamp;

	public SerializationRequest(Date timestamp) {
		this.timestamp = timestamp;
	}

	public static Event makeFeedEvent(final SignalPath signalPath) {
		Date timestamp = new Date();
		return new Event<>(new SerializationRequest(timestamp), timestamp, 0L, (serializationRequest) -> {
			SignalPathService service = Holders.getApplicationContext().getBean(SignalPathService.class);
			service.saveState(signalPath);
		});
	} 

	@Override
	public Date getTimestampAsDate() {
		return timestamp;
	}
}
