package com.unifina.feed.mongodb;

import com.unifina.domain.data.Stream;
import com.unifina.feed.json.JSONStreamrMessage;

import java.util.Date;

public class MongoMessage extends JSONStreamrMessage {

	private final Stream stream;

	public MongoMessage(String streamId, int partition, Date timestamp, Date receiveTime, DocumentFromStream map) {
		super(streamId, partition, timestamp, receiveTime, map);
		this.stream = map.getStream();
	}

	public Stream getStream() {
		return stream;
	}

}
