package com.unifina.feed.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.unifina.domain.data.Stream;
import com.unifina.utils.MapTraversal;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by henripihkala on 10/02/16.
 */
public class MongoConfigHelper {
	public static final int DEFAULT_PORT = 27017;
	public static final long DEFAULT_POLL_INTERVAL = 1000;

	public static Map<String, Object> getMongoConfig(Stream stream) {
		return (Map<String, Object>) stream.getStreamConfigAsMap().get("mongodb");
	}

	public static String getTimestampKey(Map<String, Object> mongoConfig) {
		return mongoConfig.get("timestampKey").toString();
	}

	public static long getPollIntervalMillis(Map<String, Object> mongoConfig) {
		Long interval = MapTraversal.getLong(mongoConfig, "pollIntervalMillis");
		if (interval == null)
			interval = DEFAULT_POLL_INTERVAL;
		return interval;
	}

	public static MongoClient getMongoClient(Map<String, Object> mongoConfig) {
		ServerAddress serverAddress = new ServerAddress(mongoConfig.get("host").toString(), mongoConfig.containsKey("port") ? (int) mongoConfig.get("port") : DEFAULT_PORT);
		List<MongoCredential> credentials = new ArrayList<>();
		if (mongoConfig.containsKey("username")) {
			MongoCredential credential = MongoCredential.createCredential(mongoConfig.get("username").toString(),
					mongoConfig.get("database").toString(),
					mongoConfig.containsKey("password") ? mongoConfig.get("password").toString().toCharArray() : "".toCharArray());
			credentials.add(credential);
		}

		return new MongoClient(serverAddress, credentials);
	}

	public static MongoDatabase getMongoDatabase(MongoClient mongoClient, Map<String, Object> mongoConfig) {
		return mongoClient.getDatabase(mongoConfig.get("database").toString());
	}

	public static Document getQuery(Map<String, Object> mongoConfig) {
		Document query = new Document();
		if (mongoConfig.containsKey("query"))
			query.putAll(Document.parse(mongoConfig.get("query").toString()));
		return query;
	}

	public static MongoCollection getCollection(MongoDatabase db, Map<String, Object> mongoConfig) {
		return db.getCollection(mongoConfig.get("collection").toString());
	}
}