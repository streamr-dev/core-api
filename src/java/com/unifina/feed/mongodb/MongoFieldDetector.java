package com.unifina.feed.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.unifina.domain.data.Stream;
import com.unifina.feed.FieldDetector;
import com.unifina.feed.map.MapMessage;
import org.bson.Document;

import java.util.Date;
import java.util.Map;

@SuppressWarnings("unused")
public class MongoFieldDetector extends FieldDetector {
	@Override
	protected MapMessage fetchExampleMessage(Stream stream) {

		// Connect to MongoDB and open connection
		Map<String, Object> mongoConfig = MongoConfig.getMongoConfig(stream);
		String timestampKey = MongoConfig.getTimestampKey(mongoConfig);
		MongoClient mongoClient = MongoConfig.getMongoClient(mongoConfig);
		MongoDatabase db = MongoConfig.getMongoDatabase(mongoClient, mongoConfig);
		MongoCollection collection = MongoConfig.getCollection(db, mongoConfig);

		// Build query
		Document query = MongoConfig.getQuery(mongoConfig);
		FindIterable<Document> iterable = collection.find(query).sort(Sorts.descending(timestampKey)).limit(1);
		MongoCursor<Document> mongoCursor = iterable.iterator();

		// Perform query and build MapMessage
		Document document = mongoCursor.next();
		Date timestamp = document.getDate(timestampKey);
		return new MapMessage(timestamp, timestamp, new DocumentFromStream(document, stream));
	}
}
