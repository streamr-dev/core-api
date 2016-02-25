package com.unifina.feed.mongodb

import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoCollection
import com.unifina.domain.data.Stream
import com.unifina.exceptions.InvalidStreamConfigException
import grails.validation.Validateable
import groovy.transform.CompileStatic
import org.bson.Document;

@Validateable
public class MongoDbConfig {
	public static final int DEFAULT_PORT = 27017
	public static final int DEFAULT_TIMEOUT = 1000 * 30
	public static int timeout = DEFAULT_TIMEOUT

	enum TimestampType {
		DATETIME,
		LONG

		public String getHumanReadableForm() {
			return toString().toLowerCase()
		}
	}

	String host
	Integer port = 27017
	String username
	String password
	String database
	String collection
	String timestampKey
	TimestampType timestampType = TimestampType.DATETIME

	Long pollIntervalMillis = 1000
	String query

	static constraints = {
		host(blank: false)
		port(min: 0, max: 65535)
		username(nullable: true)
		password(nullable: true)
		database(blank: false)
		collection(blank: false)
		timestampKey(blank: false)
		query(nullable: true)
	}

	static MongoDbConfig readFromStream(Stream stream) {
		def mongoMap = stream.getStreamConfigAsMap()["mongodb"]
		if (mongoMap == null)
			throw new InvalidStreamConfigException("Stream "+stream.getId()+" config does not contain the 'mongodb' key!");

		MongoDbConfig config = new MongoDbConfig(mongoMap)
		if (!config.validate())
			throw new InvalidStreamConfigException("Stream "+stream.getId()+" does not have a valid MongoDB configuration!");

		return config
	}

	static MongoDbConfig readFromStreamOrElseEmptyObject(Stream stream) {
		try {
			return readFromStream(stream)
		} catch (InvalidStreamConfigException e) {
			return new MongoDbConfig()
		}
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = [
		    host: host,
			port: port,
			database: database,
			collection: collection,
			timestampKey: timestampKey,
			timestampType: timestampType.toString(),
			pollIntervalMillis: pollIntervalMillis,
		]

		if (username) {
			map.username = username
		}
		if (password) {
			map.password = password
		}
		if (query) {
			map.query = query
		}

		return map
	}

	public MongoClient createMongoClient() {
		ServerAddress serverAddress = new ServerAddress(host, port ?: DEFAULT_PORT);
		List<MongoCredential> credentials = new ArrayList<>();

		if (username) {
			MongoCredential credential = MongoCredential.createCredential(username, database, password ? password.toCharArray() : "".toCharArray());
			credentials.add(credential);
		}

		MongoClientOptions options = MongoClientOptions
				.builder()
				.serverSelectionTimeout(timeout)
				.connectionsPerHost(1) // limit pool size to 1
				.build();

		return new MongoClient(serverAddress, credentials, options);
	}

	@CompileStatic
	public MongoCollection<Document> openCollection() {
		def client = createMongoClient()
		return client.getDatabase(database).getCollection(collection)
	}

	public Document createQuery() {
		Document document = new Document();
		if (query)
			document.putAll(Document.parse(query));
		return document;
	}

	@CompileStatic
	public Date getTimestamp(Document document) {
		Date timestamp;
		if (timestampType.equals(MongoDbConfig.TimestampType.DATETIME)) {
			timestamp = document.getDate(timestampKey);
		} else {
			timestamp = new Date(document.getLong(timestampKey));
		}
		return timestamp
	}

	@CompileStatic
	public Object convertDateToMongoFormat(Date date) {
		if (timestampType == TimestampType.LONG) {
			return date.getTime()
		} else {
			return date
		}
	}
}
