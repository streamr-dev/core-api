package com.unifina.feed.mongodb

import com.unifina.domain.data.Stream
import com.unifina.feed.map.MapMessage
import grails.test.spock.IntegrationSpec
import org.bson.Document

import java.text.DateFormat
import java.text.SimpleDateFormat

class MongoHistoricalIteratorSpec extends IntegrationSpec {

	MongoHistoricalIterator iterator
	DateFormat df

	def setup() {
		df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		df.setTimeZone(TimeZone.getTimeZone("UTC"))
	}

	def cleanup() {
		if (iterator != null) {
			iterator.close()
		}
	}

	def "it should retrieve all Documents in collection for given time span" () {
		Stream stream = new Stream(config: """ {
			"mongodb": {
				"host": "dev.streamr",
				"database": "test",
				"collection": "MongoHistoricalIteratorSpec",
				"timestampKey": "time"
			}
		}""")
		Date startDate = df.parse("2015-11-16T09:02:00.000Z")
		Date endDate = df.parse("2015-11-16T09:03:00.000Z")
		iterator = new MongoHistoricalIterator(stream, startDate, endDate)
		int count = 0

		when:
		while (iterator.hasNext()) {
			iterator.next()
			count++
		}

		then:
		count == 60
	}

	def "it should retrieve all documents if the time span exceeds what is available" () {
		Stream stream = new Stream(config: """ {
			"mongodb": {
				"host": "dev.streamr",
				"database": "test",
				"collection": "MongoHistoricalIteratorSpec",
				"timestampKey": "time"
			}
		}""")
		Date startDate = df.parse("2010-01-01T00:00:00.000Z")
		Date endDate = df.parse("2020-01-01T00:00:00.000Z")
		iterator = new MongoHistoricalIterator(stream, startDate, endDate)
		int count = 0

		when:
		while (iterator.hasNext()) {
			iterator.next()
			count++
		}

		then:
		count == 2727
	}

	def "it should use a query defined in streamConfig" () {
		Stream stream = new Stream(config: """
			{
				"mongodb": {
					"host": "dev.streamr",
					"database": "test",
					"collection": "MongoHistoricalIteratorSpec",
					"timestampKey": "time",
					"query": "{ \\"ue_id\\": ObjectId(\\"5649955225b568505b60bc31\\") }"
				}
			}
		""")
		Date startDate = df.parse("2010-01-01T00:00:00.000Z")
		Date endDate = df.parse("2020-01-01T00:00:00.000Z")
		iterator = new MongoHistoricalIterator(stream, startDate, endDate)
		int count = 0

		when:
		Date previous = null
		while (iterator.hasNext()) {
			MapMessage msg = iterator.next()
			Date date = msg.payload.getDate("time")
			if (previous != null && previous.after(date))
				throw new Exception("Events were not read in order (ascending by timestamp)!")

			previous = date
			count++
		}

		then:
		count == 184
	}

}
