package core
databaseChangeLog = {

	changeSet(author: "henripihkala (generated)", id: "1456405093736-1") {
		addColumn(tableName: "feed") {
			column(name: "data_range_provider_class", type: "varchar(255)")
		}
		sql("""
			UPDATE feed
			SET data_range_provider_class="com.unifina.feed.kafka.KafkaDataRangeProvider"
			WHERE id=7;
		""")
		sql("""
			UPDATE feed
			SET data_range_provider_class="com.unifina.feed.mongodb.MongoDataRangeProvider"
			WHERE id=8;
		""")
	}
}
