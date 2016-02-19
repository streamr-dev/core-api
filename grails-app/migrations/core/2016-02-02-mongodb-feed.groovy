package core
databaseChangeLog = {

	changeSet(author: "eric", id: "mongodb-feed-1") {
		addColumn(tableName: "feed") {
			column(name: "stream_listener_class", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "eric", id: "mongodb-feed-2") {
		addColumn(tableName: "feed") {
			column(name: "stream_page_template", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "eric", id: "mongodb-feed-3") {
		addColumn(tableName: "feed") {
			column(name: "field_detector_class", type: "varchar(255)") {
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "eric", id: "mongodb-feed-4") {
		sql("UPDATE feed SET " +
			"stream_listener_class = 'com.unifina.feed.kafka.KafkaStreamListener', " +
			"stream_page_template = 'userStreamDetails', " +
			"field_detector_class = 'com.unifina.feed.kafka.KafkaFieldDetector' " +
			"WHERE id = 7")
	}

	changeSet(author: "henri", id: "mongodb-feed-5") {
		sql("INSERT INTO `feed` (`id`, `version`, `backtest_feed`, `bundled_feed_files`, `cache_class`, `cache_config`, `directory`, `discovery_util_class`, `discovery_util_config`, `event_recipient_class`, `feed_config`, `key_provider_class`, `message_source_class`, `message_source_config`, `module_id`, `name`, `parser_class`, `preprocessor`, `realtime_feed`, `start_on_demand`, `timezone`, `stream_listener_class`, `stream_page_template`, `field_detector_class`) VALUES (NULL, '0', 'com.unifina.feed.mongodb.MongoHistoricalFeed', NULL, NULL, NULL, NULL, NULL, NULL, 'com.unifina.feed.map.MapMessageEventRecipient', NULL, 'com.unifina.feed.mongodb.MongoKeyProvider', 'com.unifina.feed.mongodb.MongoMessageSource', NULL, '147', 'MongoDB', 'com.unifina.feed.NoOpMessageParser', NULL, 'com.unifina.feed.mongodb.MongoFeed', b'1', 'UTC', 'com.unifina.feed.mongodb.MongoStreamListener', 'mongoStreamDetails', 'com.unifina.feed.mongodb.MongoFieldDetector');")
		sql("UPDATE `feed` SET `name` = 'API' WHERE `id` = '7'");
	}

	changeSet(author: "eric", id: "mongodb-feed-6-test", context: "test") {
		sql("INSERT INTO feed_user (feed_id, user_id) VALUES (8, 1)")
	}
}