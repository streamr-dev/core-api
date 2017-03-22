package com.unifina.feed.mongodb

import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.feed.DataRange
import com.unifina.feed.Message
import com.unifina.feed.MessageRecipient
import com.unifina.feed.map.MapMessage
import grails.converters.JSON
import grails.test.spock.IntegrationSpec
import org.apache.log4j.Logger
import org.bson.Document
import spock.util.concurrent.PollingConditions

import java.text.DateFormat
import java.text.SimpleDateFormat

class MongoDataRangeProviderSpec extends IntegrationSpec {

	def grailsApplication
	MongoDataRangeProvider provider
	MongoClient mongoClient
	String collectionName
	private static final Logger log = Logger.getLogger(MongoDataRangeProviderSpec)

	def setup() {
		provider = new MongoDataRangeProvider(grailsApplication)
		collectionName = this.class.getSimpleName()+System.currentTimeMillis()
	}

	def cleanup() {
		if (mongoClient != null) {
			log.info("dropping $collectionName")
			mongoClient.getDatabase("test").getCollection(collectionName).drop()
			mongoClient.close()
		}
	}

	def "it should fetch new Documents since starting" () {
		Feed feed = Feed.load(8)
		Stream stream = new Stream(name:this.class.name, user:SecUser.load(1), feed:feed)
		stream.id = "foo"

		Map config = [mongodb:[
			host: "dev.streamr",
			database: "test",
			collection: collectionName,
			timestampKey: "time",
			pollIntervalMillis: 1000
		]]
		stream.config = (config as JSON)
		stream.save()

		MongoDbConfig mongoDbConfig = MongoDbConfig.readFromStream(stream)
		mongoClient = mongoDbConfig.createMongoClient()
		MongoDatabase db = mongoClient.getDatabase(mongoDbConfig.database)

		// Produce some data into the collection
		Date firstTime = new Date()
		db.getCollection(collectionName).insertOne(
				new Document().append("time", firstTime)
		)
		Date lastTime
		for (int i=0;i<30;i++) {
			lastTime = new Date()
			db.getCollection(collectionName).insertOne(
					new Document().append("time", lastTime)
			)
		}

		when:
		DataRange range = provider.getDataRange(stream)

		then:
		range.beginDate == firstTime
		range.endDate == lastTime
	}

}
