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
import org.bson.Document
import spock.util.concurrent.PollingConditions

import java.text.DateFormat
import java.text.SimpleDateFormat

class MongoMessageSourceSpec extends IntegrationSpec {

	MongoMessageSource source
	MongoClient mongoClient
	DateFormat df

	def setup() {
		df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		df.setTimeZone(TimeZone.getTimeZone("UTC"))
	}

	def cleanup() {
		if (source != null)
			source.close()
		if (mongoClient != null)
			mongoClient.close()
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
		Stream stream = new Stream(name:"MongoMessageSourceSpec", uuid:"foo", apiKey:"foo", user:SecUser.load(1), feed:feed)

		Map config = [mongodb:[
			host: "dev.streamr",
			database: "test",
			collection: this.class.getSimpleName(),
			timestampKey: "time",
			pollIntervalMillis: 100
		]]
		stream.config = (config as JSON)
		stream.save()

		MongoDbConfig mongoDbConfig = MongoDbConfig.readFromStream(stream)
		mongoClient = mongoDbConfig.createMongoClient()
		MongoDatabase db = mongoClient.getDatabase(mongoDbConfig.database)

		when:
		source.subscribe(stream)
		Date insertTime = new Date()
		db.getCollection(this.class.getSimpleName()).insertOne(
				new Document().append("time", insertTime)
		)

		then:
		conditions.within(10) {
			assert counter == 1
			assert latestMessage?.message?.timestamp == insertTime
		}

		when:
		for (int i=0;i<5;i++) {
			insertTime = new Date()
			db.getCollection(this.class.getSimpleName()).insertOne(
					new Document().append("time", insertTime)
			)
		}

		then:
		conditions.within(10) {
			assert counter == 6
			assert latestMessage?.message?.timestamp == insertTime
		}

		when:
		source.unsubscribe(stream)
		Thread.sleep(500)

		then:
		counter == 6

	}

}
