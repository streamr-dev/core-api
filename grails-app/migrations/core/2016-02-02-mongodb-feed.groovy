package core
databaseChangeLog = {

	changeSet(author: "henri", id: "mongodb-feed-1") {
		sql("INSERT INTO `feed` (`id`, `version`, `backtest_feed`, `bundled_feed_files`, `cache_class`, `cache_config`, `directory`, `discovery_util_class`, `discovery_util_config`, `event_recipient_class`, `feed_config`, `key_provider_class`, `message_source_class`, `message_source_config`, `module_id`, `name`, `parser_class`, `preprocessor`, `realtime_feed`, `start_on_demand`, `timezone`) VALUES (NULL, '0', 'com.unifina.feed.mongodb.MongoHistoricalFeed', NULL, NULL, NULL, NULL, NULL, NULL, 'com.unifina.feed.map.MapMessageEventRecipient', NULL, '', '', NULL, '147', 'MongoDB', '', NULL, NULL, b'1', 'UTC');")
	}
}