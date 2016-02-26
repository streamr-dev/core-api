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

@SuppressWarnings("unused")
public class MongoFieldDetector extends FieldDetector {

	public MongoFieldDetector(GrailsApplication grailsApplication) {
		super(grailsApplication);
	}

	@Override
	protected MapMessage fetchExampleMessage(Stream stream) {
		MongoDbConfig config = MongoDbConfig.readFromStream(stream);

		// Build query
		try (MongoClient mongoClient = config.createMongoClient()) {
			MongoDatabase db = mongoClient.getDatabase(config.getDatabase());
			MongoCollection collection = db.getCollection(config.getCollection());

			Document query = config.createQuery();
			FindIterable<Document> iterable = collection.find(query).sort(Sorts.descending(config.getTimestampKey())).limit(1);
			MongoCursor<Document> mongoCursor = iterable.iterator();

			// Perform query and build MapMessage
			if (mongoCursor.hasNext()) {
				Document document = mongoCursor.next();
				MongoDbConfig.TimestampType timestampType = config.getTimestampType();

				Date timestamp = config.getTimestamp(document);
				// Timestamps are implicit, so remove it from the document before field detection
				document.remove(config.getTimestampKey());
				return new MapMessage(timestamp, timestamp, new DocumentFromStream(document, stream));
			} else {
				String msg = String.format("No data found %s@%s", collection.getNamespace(), mongoClient.getConnectPoint());
				throw new MongoException(msg);
			}
		}
	}
}
