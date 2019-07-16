package com.unifina.utils.testutils;

import com.streamr.client.StreamrClient;
import com.streamr.client.exceptions.ResourceNotFoundException;
import com.streamr.client.options.StreamrClientOptions;
import com.streamr.client.rest.Stream;

import java.io.IOException;
import java.util.*;

public class FakeStreamrClient extends StreamrClient {

	public Map<String, List<Map<String, Object>>> sentMessagesByChannel = new HashMap<>();

	public FakeStreamrClient(StreamrClientOptions options) {
		super(options);
	}

	@Override
	public void publish(Stream stream, Map<String, Object> payload, Date timestamp, String groupKeyHex) {
		if (!sentMessagesByChannel.containsKey(stream.getId())) {
			sentMessagesByChannel.put(stream.getId(), new ArrayList<>());
		}
		sentMessagesByChannel.get(stream.getId()).add(payload);
	}

	@Override
	public Stream getStream(String streamId) throws IOException, ResourceNotFoundException {
		Stream s = new Stream("", "");
		s.setId(streamId);
		return s;
	}
}
