package com.unifina.feed.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.unifina.domain.data.Stream;
import com.unifina.feed.map.MapMessage;
import org.bson.Document;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

/**
 * Created by henripihkala on 02/02/16.
 */
public class MongoHistoricalIterator implements Iterator<MapMessage>, Closeable {

	private final Stream stream;
	private final Date startDate;
	private final Date endDate;
	private MongoCursor<Document> mongoCursor;
	private MongoClient mongoClient;
	private String timestampKey;
	private boolean closed = false;

	public MongoHistoricalIterator(Stream stream, Date startDate, Date endDate) {
		this.stream = stream;
		this.startDate = startDate;
		this.endDate = endDate;

		validateStreamConfig(stream);
		connect();
	}

	private void connect() {
		Map<String, Object> mongoConfig = (Map<String, Object>) stream.getStreamConfigAsMap().get("mongodb");
		this.timestampKey = mongoConfig.get("timestampKey").toString();

		ServerAddress serverAddress = new ServerAddress(mongoConfig.get("host").toString(), mongoConfig.containsKey("port") ? (int) mongoConfig.get("port") : 27017);
		List<MongoCredential> credentials = new ArrayList<>();
		if (mongoConfig.containsKey("username")) {
			MongoCredential credential = MongoCredential.createCredential(mongoConfig.get("username").toString(),
					mongoConfig.get("database").toString(),
					mongoConfig.containsKey("password") ? mongoConfig.get("password").toString().toCharArray() : "".toCharArray());
			credentials.add(credential);
		}

		mongoClient = new MongoClient(serverAddress, credentials);
		MongoDatabase db = mongoClient.getDatabase(mongoConfig.get("database").toString());

		// Add static filters from streamConfig
		Document query = new Document();
		if (mongoConfig.containsKey("query"))
			query.putAll(Document.parse(mongoConfig.get("query").toString()));

		// Filter by startDate
		Document startDateFilter = new Document();
		startDateFilter.put("$gte", startDate);
		query.append(timestampKey, startDateFilter);

		// Filter by endDate
		Document endDateFilter = new Document();
		endDateFilter.put("$lte", endDate);
		query.append(timestampKey, endDateFilter);

		FindIterable<Document> iterable = db.getCollection(mongoConfig.get("collection").toString()).find(query).sort(Sorts.ascending(timestampKey));
		this.mongoCursor = iterable.iterator();
	}
	
	private void validateStreamConfig(Stream stream) {
		Map streamConfig = stream.getStreamConfigAsMap();

		if (!streamConfig.containsKey("mongodb"))
			throw new RuntimeException("Stream "+stream.getId()+" config does not contain the 'mongodb' key!");

		Map<String, Object> mongoConfig = (Map<String, Object>) streamConfig.get("mongodb");

		if (!mongoConfig.containsKey("host"))
			throw new RuntimeException("Stream "+stream.getId()+" config does not contain the 'mongodb.host' key!");
		if (!mongoConfig.containsKey("database"))
			throw new RuntimeException("Stream "+stream.getId()+" config does not contain the 'mongodb.database' key!");
		if (!mongoConfig.containsKey("collection"))
			throw new RuntimeException("Stream "+stream.getId()+" config does not contain the 'mongodb.collection' key!");
		if (!mongoConfig.containsKey("timestampKey"))
			throw new RuntimeException("Stream "+stream.getId()+" config does not contain the 'mongodb.timestampKey' key!");
	}

	public String getTimestampKey() {
		return timestampKey;
	}

	@Override
	public boolean hasNext() {
		return mongoCursor.hasNext();
	}

	@Override
	public MapMessage next() {
		Document document = mongoCursor.next();
		Date timestamp = document.getDate(timestampKey);
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
