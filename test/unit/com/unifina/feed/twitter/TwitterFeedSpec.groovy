package com.unifina.feed.twitter

import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.utils.GlobalsFactory
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class TwitterFeedSpec extends Specification {

	public static class TestableTwitterFeed extends TwitterFeed {
		TestableTwitterFeed(a, b) { super(a, b) }

		def subscribe(TwitterEventRecipient er) {
			eventRecipients.add(er)
		}
	}

	TestableTwitterFeed feed

	def setup() {
		def globals = GlobalsFactory.createInstance([:], grailsApplication, new SecUser())
		Feed twitterFeed = new Feed([
			id: 9,
			version: 0,
			eventRecipientClass: "com.unifina.feed.twitter.TwitterEventRecipient",
			keyProviderClass: "com.unifina.feed.twitter.TwitterKeyProvider",
			messageSourceClass: "com.unifina.feed.twitter.TwitterMessageSource",
			moduleId: 159,
			name: "Twitter",
			parserClass: "com.unifina.feed.twitter.TwitterMessageParser",
			realtimeFeed: "com.unifina.feed.twitter.TwitterFeed",
			startOnDemand: true,
			timezone: "UTC",
			streamListenerClass: "com.unifina.feed.twitter.TwitterStreamListener",
			streamPageTemplate: "twitterStreamDetails"
		])

		feed = new TestableTwitterFeed(globals, twitterFeed)
	}

	def eventRecipients = []

	def setupStreams(int count) {
		eventRecipients = []
		(1..count).each {
			String streamId = it.toString()
			def er = Stub(TwitterEventRecipient) {
				getStream() >> Stub(Stream) {
					getId() >> streamId
				}
			}
			eventRecipients.add(er)
			feed.subscribe(er)
		}
	}

	List<TwitterMessage> generateMessages(List<String> targetIds) {
		return targetIds.collect {
			String streamId = it
			new TwitterMessage([
				timestamp: new Date(),
				text: "tweet",
				urls: ["url1", "url2"],
				username: "tester",
				name: "TesterDude",
				language: "Und",
				followers: 3,
				streamConfig: Stub(TwitterStreamConfig) {
					getStreamId() >> streamId
				}
			])
		}
	}

	void "Feed won't send messages when there are no subscribers"() {
		def events = []
		generateMessages(["1", "2", "3"]).each {
			events.addAll(feed.process(it))
		}
		expect:
		events == []
	}

	void "Feed correctly passes messages when there's 5 subscribers"() {
		setupStreams(5)

		def events = []
		generateMessages(["1", "2", "3"]).each {
			events.addAll(feed.process(it))
		}
		expect:
		events*.recipient == [eventRecipients[0], eventRecipients[1], eventRecipients[2]]
	}
}
