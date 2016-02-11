package com.unifina.feed.mongodb

import com.unifina.domain.data.Stream
import grails.validation.Validateable;

@Validateable
public class MongoDbConfig {
	String host
	Integer port
	String username
	String password
	String database
	String collection
	String timestampKey
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
		[
		    host: host,
			port: port,
			username: username,
			password: password,
			database: database,
			collection: collection,
			timestampKey: timestampKey,
			pollIntervalMillis: pollIntervalMillis,
			query: query,
		]
	}
}
