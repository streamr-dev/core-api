package core
databaseChangeLog = {

	changeSet(author: "henri", id: "new-data-pipeline-1") {
		sql("""
			UPDATE feed SET
				realtime_feed = 'com.unifina.feed.redis.RedisFeed',
				backtest_feed = 'com.unifina.feed.cassandra.CassandraHistoricalFeed',
				discovery_util_class = NULL,
				key_provider_class = 'com.unifina.feed.StreamrBinaryMessageKeyProvider',
				message_source_class = 'com.unifina.feed.redis.MultipleRedisMessageSource',
				parser_class = 'com.unifina.feed.StreamrBinaryMessageParser',
				stream_listener_class = 'com.unifina.feed.cassandra.CassandraDeletingStreamListener',
				field_detector_class = 'com.unifina.feed.cassandra.CassandraFieldDetector',
				data_range_provider_class = 'com.unifina.feed.cassandra.CassandraDataRangeProvider'
				WHERE `id` = '7';
		""")
	}

}
