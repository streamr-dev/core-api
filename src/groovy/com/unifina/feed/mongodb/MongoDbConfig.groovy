package com.unifina.feed.mongodb

import com.unifina.domain.data.Stream
import grails.validation.Validateable;

@Validateable
public class MongoDbConfig {

	enum TimestampType {
		DATETIME,
		LONG

		public String getHumanReadableForm() {
			return toString().toLowerCase()
		}
	}

	String host
	Integer port
	String username
	String password
	String database
	String collection
	String timestampKey
	TimestampType timestampType

	Long pollIntervalMillis
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
		return mongoMap == null ? null : new MongoDbConfig(mongoMap)
	}

	def toMap() {
		Map map = [
		    host: host,
			port: port,
			database: database,
			collection: collection,
			timestampKey: timestampKey,
			timestampType: timestampType,
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
}
