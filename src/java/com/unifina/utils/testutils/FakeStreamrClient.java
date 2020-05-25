package com.unifina.utils.testutils;

import com.streamr.client.StreamrClient;
import com.streamr.client.exceptions.ResourceNotFoundException;
import com.streamr.client.options.StreamrClientOptions;
import com.streamr.client.rest.Stream;
import com.streamr.client.rest.UserInfo;
import com.streamr.client.utils.UnencryptedGroupKey;

import java.io.IOException;
import java.util.*;

/**
 * Helper class to be used in unit tests which use StreamrClient.
 * Aims to prevent actual http/websocket connections from being opened.
 */
public class FakeStreamrClient extends StreamrClient {

	private Map<String, List<SentMessage>> sentMessagesByChannel = new HashMap<>();

	StreamrClientOptions optionsPassedToConstructor;

	public FakeStreamrClient(StreamrClientOptions options) {
		super(options);
	}

	@Override
	public void publish(Stream stream, Map<String, Object> payload, Date timestamp, String partitionKey, UnencryptedGroupKey groupKey) {
		if (!sentMessagesByChannel.containsKey(stream.getId())) {
			sentMessagesByChannel.put(stream.getId(), new ArrayList<>());
		}
		sentMessagesByChannel.get(stream.getId()).add(new SentMessage(payload, timestamp, partitionKey));
	}

	@Override
	public Stream getStream(String streamId) throws IOException, ResourceNotFoundException {
		Stream s = new Stream("", "");
		s.setId(streamId);
		return s;
	}

	@Override
	public UserInfo getUserInfo() throws IOException {
		return new UserInfo("test-user", "test-username", "test-id");
	}

	public Map<String, List<SentMessage>> getAndClearSentMessages() {
		Map<String, List<SentMessage>> result = sentMessagesByChannel;
		sentMessagesByChannel = new HashMap<>();
		return result;
	}

	static class SentMessage {
		public Map<String, Object> payload;
		public Date timestamp;
		public String partitionKey;

		public SentMessage(Map<String, Object> payload, Date timestamp, String partitionKey) {
			this.payload = payload;
			this.timestamp = timestamp;
			this.partitionKey = partitionKey;
		}
	}
}
