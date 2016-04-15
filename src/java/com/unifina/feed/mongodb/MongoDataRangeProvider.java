package com.unifina.feed.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.unifina.domain.data.Stream;
import com.unifina.feed.AbstractDataRangeProvider;
import com.unifina.feed.DataRange;
import org.bson.Document;
import org.codehaus.groovy.grails.commons.GrailsApplication;

/**
 * Created by henripihkala on 25/02/16.
 */
public class MongoDataRangeProvider extends AbstractDataRangeProvider {

	public MongoDataRangeProvider(GrailsApplication grailsApplication) {
		super(grailsApplication);
	}

	@Override
	public DataRange getDataRange(Stream stream) {
		DataRange range = null;

		MongoDbConfig config = MongoDbConfig.readFromStream(stream);
		MongoClient mongoClient = config.createMongoClient();

		try {
			MongoDatabase db = mongoClient.getDatabase(config.getDatabase());
			MongoCollection collection = db.getCollection(config.getCollection());

			Document query = config.createQuery();

			FindIterable<Document> firstDocument = collection.find(query).sort(Sorts.ascending(config.getTimestampKey())).limit(1);
			MongoCursor<Document> firstIterator = firstDocument.iterator();
			FindIterable<Document> lastDocument = collection.find(query).sort(Sorts.descending(config.getTimestampKey())).limit(1);
			MongoCursor<Document> lastIterator = lastDocument.iterator();

			if (firstIterator.hasNext() && lastIterator.hasNext()) {
				range = new DataRange(config.getTimestamp(firstIterator.next()), config.getTimestamp(lastIterator.next()));
			}
			return range;
		} finally {
			mongoClient.close();
		}
	}
}
