package com.unifina.signalpath.streams;

import com.unifina.domain.data.Stream;
import com.unifina.service.StreamService;
import com.unifina.signalpath.BooleanOutput;
import com.unifina.signalpath.Output;

import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetOrCreateStream extends CreateStream {

	private transient StreamService streamService;

	@Override
	public void sendOutput() {
		if (streamService == null) {
			streamService = getGlobals().getBean(StreamService.class);
		}

		Stream stream = streamService.findByName(getStreamName());
		if (stream == null) {
			// create new stream: delegate to CreateStream module
			//   surely it now will sendOutputs with created==true (it can't have cached a name streamService doesn't know of)
			super.sendOutput();
		} else {
			sendOutputs(false, stream);
		}
	}
}
