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
import org.codehaus.groovy.grails.commons.GrailsApplication;

import java.util.Date;
import java.util.Map;

@SuppressWarnings("unused")
public class MongoFieldDetector extends FieldDetector {

	public MongoFieldDetector(GrailsApplication grailsApplication) {
		super(grailsApplication);
	}

	@Override
	protected MapMessage fetchExampleMessage(Stream stream) {

		// Connect to MongoDB and open connection
		Map<String, Object> mongoConfig = MongoConfigHelper.getMongoConfig(stream);
		String timestampKey = MongoConfigHelper.getTimestampKey(mongoConfig);
		MongoClient mongoClient = MongoConfigHelper.getMongoClient(mongoConfig);
		MongoDatabase db = MongoConfigHelper.getMongoDatabase(mongoClient, mongoConfig);
		MongoCollection collection = MongoConfigHelper.getCollection(db, mongoConfig);

		// Build query
		Document query = MongoConfigHelper.getQuery(mongoConfig);
		FindIterable<Document> iterable = collection.find(query).sort(Sorts.descending(timestampKey)).limit(1);
		MongoCursor<Document> mongoCursor = iterable.iterator();

		// Perform query and build MapMessage
		Document document = mongoCursor.next();
		Date timestamp = document.getDate(timestampKey);
		return new MapMessage(timestamp, timestamp, new DocumentFromStream(document, stream));
	}
}
