package com.unifina.utils.testutils;

import com.streamr.client.StreamrClient;
import com.streamr.client.exceptions.ResourceNotFoundException;
import com.streamr.client.options.StreamrClientOptions;
import com.streamr.client.rest.Stream;

import java.io.IOException;
import java.util.*;

public class FakeStreamrClient extends StreamrClient {

	private Map<String, List<SentMessage>> sentMessagesByChannel = new HashMap<>();

	public FakeStreamrClient(StreamrClientOptions options) {
		super(options);
	}

	@Override
	public void publish(Stream stream, Map<String, Object> payload, Date timestamp, String partitionKey, String groupKeyHex) {
		if (!sentMessagesByChannel.containsKey(stream.getId())) {
			sentMessagesByChannel.put(stream.getId(), new ArrayList<>());
		}
		sentMessagesByChannel.get(stream.getId()).add(new SentMessage(payload, timestamp, partitionKey, groupKeyHex));
	}

	@Override
	public Stream getStream(String streamId) throws IOException, ResourceNotFoundException {
		Stream s = new Stream("", "");
		s.setId(streamId);
		return s;
	}

	public Map<String, List<SentMessage>> getAndClearSentMessages() {
		Map<String, List<SentMessage>> result = sentMessagesByChannel;
		sentMessagesByChannel = new HashMap<>();
		return result;
	}

	class SentMessage {
		public Map<String, Object> payload;
		public Date timestamp;
		public String partitionKey;
		public String groupKeyHex;

		public SentMessage(Map<String, Object> payload, Date timestamp, String partitionKey, String groupKeyHex) {
			this.payload = payload;
			this.timestamp = timestamp;
			this.partitionKey = partitionKey;
			this.groupKeyHex = groupKeyHex;
		}
	}
}
