package com.unifina.feed
import spock.lang.Specification


class MessageHubSpec extends Specification {

	MessageHub hub
	MessageSource source

	def setup() {
		source = Mock(MessageSource)
		hub = new MessageHub(source, null)
		hub.start()
	}

	def cleanup() {
		hub.quit()
	}

	def "test subscribe"() {
		def recipient = Mock(MessageRecipient)
		Message msg = new Message("key", new Object())

		when: "the recipient subscribes"
			hub.subscribe("key", recipient)
		then: "the source must subscribe"
			1 * source.subscribe("key")

		when: "hub gets a message"
			synchronized (hub) {
				hub.receive(msg)
				hub.wait(500)
			}
		then: "message must be passed on to recipient"
			1 * recipient.receive(_)
	}

	def "test multiple recipients"() {
		def recipient1 = Mock(MessageRecipient)
		def recipient2 = Mock(MessageRecipient)
		Message msg = new Message("key", new Object())

		when: "both recipients subscribe"
			hub.subscribe("key", recipient1)
			hub.subscribe("key", recipient2)
		then: "the source must subscribe a least once"
			(1..2) * source.subscribe("key")

		when: "hub gets a message"
			synchronized (hub) {
				hub.receive(msg)
				hub.wait(500)
			}
		then: "message must be passed on to both recipients"
			1 * recipient1.receive(_)
			1 * recipient2.receive(_)

		when: "first recipient unsubscribes"
			hub.unsubscribe("key", recipient1)
		then: "source must not yet be unsubscribed"
			0 * source.unsubscribe("key")

		when: "the second recipient unsubscribes"
			hub.unsubscribe("key", recipient2)
		then: "source must be unsubscribed"
			1 * source.unsubscribe("key")
	}

}
