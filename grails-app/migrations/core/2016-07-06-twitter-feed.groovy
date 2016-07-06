package core
databaseChangeLog = {

	changeSet(author: "jtakalai", id: "2016-07-06-twitter-feed-1") {
		insert(tableName: "feed") {
			column(name: "id", valueNumeric: 9)
			column(name: "version", valueNumeric: 0)
			column(name: "event_recipient_class", value: "com.unifina.feed.twitter.TwitterEventRecipient")
			column(name: "key_provider_class", value: "com.unifina.feed.twitter.TwitterKeyProvider")
			column(name: "message_source_class", value: "com.unifina.feed.twitter.TwitterMessageSource")
			column(name: "module_id", valueNumeric: 159)
			column(name: "name", value: "Twitter")
			column(name: "parser_class", value: "com.unifina.feed.twitter.TwitterMessageParser")
			column(name: "realtime_feed", value: "com.unifina.feed.twitter.TwitterFeed")
			column(name: "start_on_demand", valueBoolean: true)
			column(name: "timezone", value: "UTC")
			column(name: "stream_listener_class", value: "com.unifina.feed.twitter.TwitterStreamListener")
			column(name: "stream_page_template", value: "twitterStreamDetails")
		}
	}
}