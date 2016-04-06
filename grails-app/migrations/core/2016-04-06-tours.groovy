package core

import org.json.JSONObject

databaseChangeLog = {

	changeSet(author: "henri", id: "tour-resources") {
		// Rename Charts module category to Visualizations
		grailsChange {
			change {
				sql.execute("UPDATE module_category SET name = 'Visualizations' WHERE name = 'Charts'")
			}
		}

		// Insert demo stream
		insert(tableName: "stream") {
			column(name: "version", valueNumeric: 0)
			column(name: "id", value: "YpTAPDbvSAmj-iCUYz-dxA")
			column(name: "api_key", value: "RYZ2idC0RZ2mGyRJARiBaQ")
			column(name: "name", value: "Public transport demo")
			column(name: "description", value: "Helsinki tram locations etc.")
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

		// TODO: insert public read permission
	}

}