package com.unifina.utils.testutils;

import com.unifina.domain.data.Stream;
import com.unifina.service.StreamService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeStreamService extends StreamService {

	public Map<String, List<Map>> receivedContentByChannel = new HashMap<>();

	@Override
	public void sendMessage(Stream stream, Map message) {
		String c = stream.getId();
		if (!receivedContentByChannel.containsKey(c)) {
			receivedContentByChannel.put(c, new ArrayList<Map>());
		}
		receivedContentByChannel.get(c).add(message);
	}

}
