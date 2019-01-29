package com.unifina.signalpath;

import com.streamr.client.protocol.message_layer.ITimestamped;
import java.util.Date;

public class StopRequest implements ITimestamped {

	private final Date date;

	public StopRequest(Date date) {
		this.date = date;
	}

	@Override
	public Date getTimestampAsDate() {
		return date;
	}
}
