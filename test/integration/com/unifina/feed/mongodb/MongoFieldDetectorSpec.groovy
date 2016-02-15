package com.unifina.feed.mongodb

import com.mongodb.MongoClient
import com.mongodb.MongoTimeoutException
import com.mongodb.client.MongoDatabase
import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.feed.FieldDetector
import com.unifina.utils.IdGenerator
import grails.converters.JSON
import grails.test.spock.IntegrationSpec
import groovy.transform.CompileStatic
import org.bson.Document

class MongoFieldDetectorSpec extends IntegrationSpec {
	final String collectionName = getClass().simpleName

	def grailsApplication
	def streamService

	FieldDetector fieldDetector
	Stream stream

	def setupSpec() {
		MongoConfigHelper.timeout = 1000
	}

	def cleanupSpec() {
		MongoConfigHelper.timeout = MongoConfigHelper.DEFAULT_TIMEOUT
	}

	def setup() {
		fieldDetector = new MongoFieldDetector(grailsApplication)
		stream = streamService.createStream([name: "mongoFieldDetectorSpec-stream", feed: Feed.MONGO_ID],
			SecUser.load(1), null)
	}

	def cleanup() {
		stream.delete()
	}

	def "throws exception given no connection details"() {
		when:
		fieldDetector.detectFields(stream)

		then:
		thrown(NullPointerException) // TODO: better exception
	}

	def "throws exception if invalid host"() {
		configureStreamWith([
			host: "nonexistent.streamr",
			port: 27017,
			database: "test",
			collection: collectionName,
			timestampKey: "time",
			timestampType: MongoDbConfig.TimestampType.DATETIME
		])

		when:
		fieldDetector.detectFields(stream)

		then:
		thrown(MongoTimeoutException)
	}

	def "throws exception if non-existent database"() {
		configureStreamWith([
			host: "dev.streamr",
			port: 27017,
			database: "test" + IdGenerator.get(),
			collection: collectionName,
			timestampKey: "time",
			timestampType: MongoDbConfig.TimestampType.DATETIME
		])

		when:
		fieldDetector.detectFields(stream)

		then:
		thrown(MongoException)
	}

	def "throws exception if non-existent collection"() {
		configureStreamWith([
			host: "dev.streamr",
			port: 27017,
			database: "test",
			collection: collectionName + IdGenerator.get(),
			timestampKey: "time",
			timestampType: MongoDbConfig.TimestampType.DATETIME
		])

		when:
		fieldDetector.detectFields(stream)

		then:
		thrown(MongoException)
	}

	def "throws exception if collection is empty"() {
		def db = openDbWith([
			host: "dev.streamr",
			port: 27017,
			database: "test",
			collection: collectionName,
			timestampKey: "time",
			timestampType: MongoDbConfig.TimestampType.DATETIME
		])

		def collection = db.getCollection(collectionName)
		collection.drop()
		db.createCollection(collectionName)

		when:
		fieldDetector.detectFields(stream)

		then:
		thrown(MongoException)

		cleanup:
		db.getCollection(getClass().getSimpleName()).drop()
	}

	def "uses latest (w.r.t timestamp) data entry to detect fields"() {
		def db = openDbWith([
			host: "dev.streamr",
			port: 27017,
			database: "test",
			collection: getClass().simpleName,
			timestampKey: "time",
			timestampType: MongoDbConfig.TimestampType.DATETIME
		])

		def collection = db.getCollection(collectionName)
		collection.insertOne(new Document([a: 0, b: 666, time: new Date(10)]))
		collection.insertOne(new Document([a: 3, b: 666, time: new Date(100), d: "hello world"]))
		collection.insertOne(new Document([a: 5, b: 13, time: new Date(0)]))
		collection.insertOne(new Document([a: "hello", b: "world"]))

		when:
		def result = fieldDetector.detectFields(stream)

		then:
		result == [
			[name: "_id", type: "string"],
			[name: "a", type: "number"],
			[name: "b", type: "number"],
			[name: "time", type: "string"],
			[name: "d", type: "string"]
		]
	}

	void configureStreamWith(Map map) {
		def mongoDbConfig = new MongoDbConfig(map)
		Map config = stream.getStreamConfigAsMap()
		config.mongodb = mongoDbConfig.toMap()
		stream.config = config as JSON
		stream.save(failOnError: true)
	}

	@CompileStatic
	MongoDatabase openDbWith(Map map) {
		configureStreamWith(map)
		Map<String, Object> mongoConfig = MongoConfigHelper.getMongoConfig(stream);
		MongoClient mongoClient = MongoConfigHelper.getMongoClient(mongoConfig);
		return MongoConfigHelper.getMongoDatabase(mongoClient, mongoConfig);
	}
}
