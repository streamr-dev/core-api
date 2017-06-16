package com.unifina.feed.mongodb

import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
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

class MongoMessageSourceSpec extends IntegrationSpec {

	MongoMessageSource source
	MongoClient mongoClient
	DateFormat df
	String collectionName
	private static final Logger log = Logger.getLogger(MongoMessageSourceSpec)

	def setup() {
		df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		df.setTimeZone(TimeZone.getTimeZone("UTC"))
		collectionName = this.class.getSimpleName()+System.currentTimeMillis()
		log.info("collectionName: $collectionName")
	}

	def cleanup() {
		if (source != null)
			source.close()
		if (mongoClient != null) {
			log.info("dropping $collectionName")
			mongoClient.getDatabase("test").getCollection(collectionName).drop()
			mongoClient.close()
		}
	}

	def "it should fetch new Documents since starting" () {
		def conditions = new PollingConditions()
		Feed feed = Feed.load(8)
		source = new MongoMessageSource(feed, [:])
		int counter = 0
		Message<MapMessage, Stream> latestMessage = null
		source.recipient = new MessageRecipient<MapMessage, Stream>() {
			@Override
			void receive(Message<MapMessage, Stream> message) {
				counter++
				latestMessage = message
			}

			@Override
			void sessionBroken() {

			}

			@Override
			void sessionRestored() {

			}

			@Override
			void sessionTerminated() {

			}

			@Override
			int getReceivePriority() {
				return 0
			}
		}
		Stream stream = new Stream(name:"MongoMessageSourceSpec", user:SecUser.load(1), feed:feed)
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

		when:
		source.subscribe(stream)
		Thread.sleep(100)
		Date insertTime = new Date()
		log.info("Inserting ${insertTime.time}")
		db.getCollection(collectionName).insertOne(
				new Document().append("time", insertTime)
		)

		then:
		conditions.within(10) {
			log.info("Asserting 1 message. counter: $counter, latestMessage: $latestMessage")
			assert counter == 1
			assert latestMessage?.message?.timestamp == insertTime
			assert !latestMessage.checkCounter
		}

		when:
		for (int i=0;i<5;i++) {
			insertTime = new Date()
			log.info("Inserting $insertTime")
			db.getCollection(collectionName).insertOne(
					new Document().append("time", insertTime)
			)
		}

		then:
		conditions.within(10) {
			log.info("Asserting 6 messages. counter: $counter, latestMessage: $latestMessage")
			assert counter == 6
			assert latestMessage?.message?.timestamp == insertTime
		}

		when:
		source.unsubscribe(stream)
		Thread.sleep(2000) // sleep for longer than the poll interval to see if any extra messages come through

		then:
		counter == 6

	}

}
