package com.unifina.feed

import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class PollingMessageSourceSpec extends Specification {

	List<PollingMessageSource<String,String>> sources = []

	def setup() {
		sources.clear()
	}

	def cleanup() {
		sources*.close()
		sources.clear()
	}

	def "subscribe starts the polling at correct intervals"() {
		def conditions = new PollingConditions()
		int counter = 0

		sources << new PollingMessageSource<String,String>(null, null) {
			@Override
			protected Poller createPoller(String subscriber) {
				return new Poller() {
					@Override
					List<Message<String,String>> poll() {
						counter++
						return []
					}

					@Override
					long getPollInterval() {
						return 100
					}

					@Override
					void close() {

					}
				}
			}
		}


		when:
		sources[0].subscribe("foo")
		then:
		conditions.within(2) {
			assert counter >= 10 && counter < 20
		}
	}

	def "polled messages get forwarded"() {
		def conditions = new PollingConditions()
		int msgCounter = 0
		int fwdCounter = 0
		sources << new PollingMessageSource<String,String>(null, null) {
			@Override
			protected Poller createPoller(String subscriber) {
				return new Poller() {
					@Override
					List<Message<String,String>> poll() {
						return [new Message<String, String>("foo", msgCounter++, "")]
					}

					@Override
					long getPollInterval() {
						return 100
					}

					@Override
					void close() {

					}
				}
			}
		}
		sources[0].recipient = new MessageRecipient<String, String>() {
			@Override
			void receive(Message<String, String> message) {
				fwdCounter++
			}

			@Override
			void sessionBroken() {

			}

			@Override
			void sessionRestored() {

			}

			@Override
			void sessionTerminated() {

			}

			@Override
			int getReceivePriority() {
				return 0
			}
		}


		when:
		sources[0].subscribe("foo")
		then:
		conditions.within(2) {
			assert msgCounter >= 10 && msgCounter < 20
		}

		when:
		sources[0].unsubscribe("foo")
		then:
		msgCounter == fwdCounter

	}

	def "multiple subscribers for one source"() {
		def conditions = new PollingConditions()
		int fooCounter = 0
		int barCounter = 0
		boolean fooClosed = false
		boolean barClosed = false
		sources << new PollingMessageSource<String,String>(null, null) {
			@Override
			protected Poller createPoller(String subscriber) {
				return new Poller() {
					@Override
					List<Message> poll() {
						if (subscriber == "foo")
							fooCounter++
						else barCounter++

						return []
					}

					@Override
					long getPollInterval() {
						if (subscriber=="foo")
							return 100
						else
							return 200
					}

					@Override
					void close() {
						if (subscriber == "foo")
							fooClosed = true
						else barClosed = true
					}
				}
			}
		}


		when:
		sources[0].subscribe("foo")
		sources[0].subscribe("bar")

		then:
		conditions.within(2) {
			assert fooCounter >= 10 && fooCounter < 20
			assert barCounter >= 5 && barCounter < fooCounter
		}

		when: "unsubscribing foo"
		sources[0].unsubscribe("foo")
		def fooCounterAfterUnsubscribe = fooCounter
		def barCounterAfterUnsubscribe = barCounter

		then: "foo must be closed and bar not closed"
		fooClosed
		!barClosed

		when: "time passes"
		Thread.sleep(500)

		then: "foo must not be incremented anymore, but bar must be incremented"
		fooCounter == fooCounterAfterUnsubscribe
		barCounter > barCounterAfterUnsubscribe
	}

	def "multiple sources, same sub"() {
		def conditions = new PollingConditions()
		int counter0 = 0
		int counter1 = 0
		sources << new PollingMessageSource<String,String>(null, null) {
			@Override
			protected Poller createPoller(String subscriber) {
				return new Poller() {
					@Override
					List<Message> poll() {
						counter0++
						return []
					}

					@Override
					long getPollInterval() {
						return 100
					}

					@Override
					void close() {

					}
				}
			}
		}
		sources << new PollingMessageSource<String,String>(null, null) {
			@Override
			protected Poller createPoller(String subscriber) {
				return new Poller() {
					@Override
					List<Message> poll() {
						counter1++
						return []
					}

					@Override
					long getPollInterval() {
						return 200
					}

					@Override
					void close() {

					}
				}
			}
		}


		when:
		sources[0].subscribe("foo")
		sources[1].subscribe("foo")
		then:
		conditions.within(2) {
			assert counter0 >= 10 && counter0 < 20
			assert counter1 >= 5 && counter1 < counter0
		}
	}
	
}
