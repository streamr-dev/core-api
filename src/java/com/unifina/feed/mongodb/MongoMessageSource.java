package com.unifina.feed.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.unifina.domain.data.Feed;
import com.unifina.domain.data.Stream;
import com.unifina.feed.Message;
import com.unifina.feed.Poller;
import com.unifina.feed.PollingMessageSource;
import com.unifina.feed.map.MapMessage;
import org.bson.Document;

import java.util.*;

/**
 * Created by henripihkala on 09/02/16.
 */
public class MongoMessageSource extends PollingMessageSource<MapMessage, Stream> {

	public MongoMessageSource(Feed feed, Map<String, Object> config) {
		super(feed, config);
	}

	@Override
	protected Poller createPoller(Stream subscriber) {
		final Stream stream = subscriber;
		return new Poller() {

			private final Map<String, Object> mongoConfig = MongoConfig.getMongoConfig(stream);
			private final String timestampKey = MongoConfig.getTimestampKey(mongoConfig);
			private final MongoClient mongoClient = MongoConfig.getMongoClient(mongoConfig);
			private final MongoDatabase db = MongoConfig.getMongoDatabase(mongoClient, mongoConfig);
			private final MongoCollection collection = MongoConfig.getCollection(db, mongoConfig);

			// Add static filters from mongoConfig
			private final Document startDateFilter = new Document();
			private final Document query = MongoConfig.getQuery(mongoConfig).append(timestampKey, startDateFilter);
			private Date lastDate = new Date();
			private long counter = 0;

			@Override
			public long getPollInterval() {
				return MongoConfig.getPollIntervalMillis(mongoConfig);
			}

			@Override
			public void close() {
				mongoClient.close();
			}

			@Override
			public List<Message<MapMessage, Stream>> poll() {

				// Update the date filter
				startDateFilter.put("$gt", lastDate);

				List<Message<MapMessage, Stream>> list = new LinkedList<>();
				FindIterable<Document> iterable = collection.find(query).sort(Sorts.ascending(timestampKey));
				for (Document document : iterable) {
					Date timestamp = document.getDate(timestampKey);
					MapMessage mapMsg = new MapMessage(timestamp, new Date(), new DocumentFromStream(document, stream));
					Message<MapMessage, Stream> msg = new Message<>(stream, counter++, mapMsg);
					list.add(msg);
					lastDate = timestamp;
				}

				return list;
			}
		};
	}

}
