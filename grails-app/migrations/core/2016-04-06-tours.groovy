package core

import org.apache.log4j.Logger
import org.json.JSONObject

Logger log = Logger.getLogger("tours")

databaseChangeLog = {

	changeSet(author: "henri", id: "tour-resources-1") {
		// Rename Charts module category to Visualizations
		grailsChange {
			change {
				sql.execute("UPDATE module_category SET name = 'Visualizations' WHERE name = 'Charts'")
			}
		}
	}

	changeSet(author: "henri", id: "tour-resources-2", failOnError: false) {

		String now = "2016-05-18T18:06:00"

		// Insert demo stream
		insert(tableName: "stream") {
			column(name: "version", valueNumeric: 0)
			column(name: "id", value: "YpTAPDbvSAmj-iCUYz-dxA")
			column(name: "api_key", value: "RYZ2idC0RZ2mGyRJARiBaQ")
			column(name: "name", value: "Public transport demo")
			column(name: "description", value: "Helsinki tram locations etc.")
			column(name: "date_created", valueDate: now)
			column(name: "last_updated", valueDate: now)
			column(name: "feed_id", valueNumeric: 7) // API stream

			def config = [fields: [
				[name: "veh", type: "string"],
				[name: "lat", type: "number"],
				[name: "long", type: "number"],
				[name: "spd", type: "number"],
				[name: "hdg", type: "number"],
				[name: "odo", type: "number"],
				[name: "dl", type: "number"],
				[name: "desi", type: "string"]
			]]
			column(name: "config", value: new JSONObject(config).toString())
			column(name: "user_id", valueNumeric: 1)
			column(name: "class", value: "com.unifina.domain.data.Stream")
		}
	}

	changeSet(author: "henri", id: "tour-resources-3", failOnError: false) {

		String now = "2016-05-31T18:16:00"

		// Insert demo stream
		insert(tableName: "stream") {
			column(name: "version", valueNumeric: 0)
			column(name: "id", value: "ln2g8OKHSdi7BcL-bcnh2g")
			column(name: "api_key", value: "TaPRLN84RXqh8HXuFjQDLg")
			column(name: "name", value: "Twitter-Bitcoin")
			column(name: "description", value: "Bitcoin mentions on Twitter")
			column(name: "date_created", valueDate: now)
			column(name: "last_updated", valueDate: now)
			column(name: "feed_id", valueNumeric: 7) // API stream

			def config = [fields: [
				[name: "text", type: "string"],
				[name: "user", type: "object"],
				[name: "retweet_count", type: "number"],
				[name: "favorite_count", type: "number"],
				[name: "lang", type: "string"]
			]]
			column(name: "config", value: new JSONObject(config).toString())
			column(name: "user_id", valueNumeric: 1)
			column(name: "class", value: "com.unifina.domain.data.Stream")
		}
	}

	changeSet(author: "henri", id: "tour-resources-4") {

		// Grant public read permission to demo streams
		insert(tableName: "permission") {
			column(name: "version", valueNumeric: 0)
			column(name: "clazz", value: "com.unifina.domain.data.Stream")
			column(name: "operation", value: "read")
			column(name: "string_id", value: "YpTAPDbvSAmj-iCUYz-dxA")
			column(name: "anonymous", valueBoolean: false)
		}
		insert(tableName: "permission") {
			column(name: "version", valueNumeric: 0)
			column(name: "clazz", value: "com.unifina.domain.data.Stream")
			column(name: "operation", value: "read")
			column(name: "string_id", value: "ln2g8OKHSdi7BcL-bcnh2g")
			column(name: "anonymous", valueBoolean: false)
		}

		// Insert feed files
		insert(tableName: "feed_file") {
			column(name: "begin_date", value: "2016-04-11 00:00:00")
			column(name: "day", value: "2016-04-11 00:00:00")
			column(name: "end_date", value: "2016-04-11 23:59:59")
			column(name: "feed_id", valueNumeric: 7)
			column(name: "name", value: "kafka.20160411.YpTAPDbvSAmj-iCUYz-dxA.gz")
			column(name: "process_task_created", valueNumeric: 0)
			column(name: "processed", valueNumeric: 1)
			column(name: "processing", valueNumeric: 0)
			column(name: "stream_id", value: "YpTAPDbvSAmj-iCUYz-dxA")
		}
		insert(tableName: "feed_file") {
			column(name: "begin_date", value: "2016-04-12 00:00:00")
			column(name: "day", value: "2016-04-12 00:00:00")
			column(name: "end_date", value: "2016-04-12 23:59:59")
			column(name: "feed_id", valueNumeric: 7)
			column(name: "name", value: "kafka.20160412.YpTAPDbvSAmj-iCUYz-dxA.gz")
			column(name: "process_task_created", valueNumeric: 0)
			column(name: "processed", valueNumeric: 1)
			column(name: "processing", valueNumeric: 0)
			column(name: "stream_id", value: "YpTAPDbvSAmj-iCUYz-dxA")
		}
	}

	changeSet(author: "henri", id: "tour-resources-5") {
		// Grant public read permission to demo streams
		update(tableName: "permission") {
			column(name: "anonymous", valueBoolean: true)
			where("string_id = 'YpTAPDbvSAmj-iCUYz-dxA' and operation = 'read'")
		}
		update(tableName: "permission") {
			column(name: "anonymous", valueBoolean: true)
			where("string_id = 'ln2g8OKHSdi7BcL-bcnh2g' and operation = 'read'")
		}
	}

	// Complete the tours for test users to avoid screwing up func tests etc.
	changeSet(author: "henri", id: "tours-completed", context: "test") {
		def date = "2016-04-11T15:00:00"

		// tester1
		(0..2).each { num ->
			insert(tableName: "tour_user") {
				column(name: "user_id", valueNumeric: 1)
				column(name: "tour_number", valueNumeric: num)
				column(name: "completed_at", valueDate: date)
			}
			// tester2
			insert(tableName: "tour_user") {
				column(name: "user_id", valueNumeric: 2)
				column(name: "tour_number", valueNumeric: num)
				column(name: "completed_at", valueDate: date)
			}
			// tester-admin
			insert(tableName: "tour_user") {
				column(name: "user_id", valueNumeric: 3)
				column(name: "tour_number", valueNumeric: num)
				column(name: "completed_at", valueDate: date)
			}
		}
	}

}