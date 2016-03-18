package com.unifina.feed.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.unifina.domain.data.Stream;
import com.unifina.feed.map.MapMessage;
import org.bson.Document;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

public class MongoHistoricalIterator implements Iterator<MapMessage>, Closeable {

	private final Stream stream;
	private final Date startDate;
	private final Date endDate;
	private MongoCursor<Document> mongoCursor;
	private MongoClient mongoClient;
	private MongoDbConfig config;
	private boolean closed = false;

	public MongoHistoricalIterator(Stream stream, Date startDate, Date endDate) {
		this.stream = stream;
		this.startDate = startDate;
		this.endDate = endDate;

		connect();
	}

	private void connect() {
		config = MongoDbConfig.readFromStream(stream);
		mongoClient = config.createMongoClient();
		MongoDatabase db = mongoClient.getDatabase(config.getDatabase());
		MongoCollection collection = db.getCollection(config.getCollection());

		// Add static filters from mongoConfig
		Document query = config.createQuery();

		// Filter by time range
		Document timeFilter = new Document();
		timeFilter.put("$gte", config.convertDateToMongoFormat(startDate));
		timeFilter.put("$lte", config.convertDateToMongoFormat(endDate));
		query.append(config.getTimestampKey(), timeFilter);

		FindIterable<Document> iterable = collection.find(query).sort(Sorts.ascending(config.getTimestampKey()));
		this.mongoCursor = iterable.iterator();
	}

	public String getTimestampKey() {
		return config.getTimestampKey();
	}

	@Override
	public boolean hasNext() {
		return mongoCursor.hasNext();
	}

	@Override
	public MapMessage next() {
		Document document = mongoCursor.next();
		Date timestamp = config.getTimestamp(document);
		return new MapMessage(timestamp, timestamp, new DocumentFromStream(document, stream));
	}

	@Override
	public void remove() {
		mongoCursor.remove();
	}

	@Override
	public void close() throws IOException {
		mongoCursor.close();
		mongoClient.close();
		closed = true;
	}

	public boolean isClosed() {
		return closed;
	}
}
