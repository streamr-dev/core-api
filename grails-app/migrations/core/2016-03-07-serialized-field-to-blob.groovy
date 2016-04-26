package core

databaseChangeLog = {

	changeSet(author: "eric", id: "serialized-feed-to-blob-1") {
		sql("UPDATE canvas SET serialized = NULL, serialization_time = NULL")
		modifyDataType(columnName: "serialized", newDataType: "mediumblob", tableName: "canvas")
	}

	changeSet(author: "eric", context: "test", id: "serialized-feed-to-blob-2") {
		grailsChange {
			change {
				byte[] bytes = new byte[512]
				new Random(1234).nextBytes(bytes)
				sql.executeUpdate("""
					UPDATE canvas
					SET
						serialization_time = NOW(),
						serialized = ?
					WHERE
						name = 'BrokenSerialization'""", [bytes])
			}
		}
	}
}
