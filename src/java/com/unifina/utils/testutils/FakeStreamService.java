package com.unifina.utils.testutils;

import com.streamr.client.protocol.message_layer.StreamMessage;
import com.unifina.domain.data.Stream;
import com.unifina.service.StreamService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeStreamService extends StreamService {

	public Map<String, List<StreamMessage>> sentMessagesByChannel = new HashMap<>();

	@Override
	public void sendMessage(StreamMessage msg) {
		String c = msg.getStreamId();
		if (!sentMessagesByChannel.containsKey(c)) {
			sentMessagesByChannel.put(c, new ArrayList<StreamMessage>());
		}
		sentMessagesByChannel.get(c).add(msg);
	}

}
