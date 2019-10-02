package com.unifina.feed.util


import com.streamr.client.protocol.message_layer.StreamMessage
import com.streamr.client.subs.Subscription
import spock.lang.Specification

class IterableMessageHandlerSpec extends Specification {

	StreamMessage msg
	Subscription sub
	IterableMessageHandler iterator

	// Tweak these if getting test flakiness
	private static final long ASYNC_DELAY_MILLIS = 500 // should be short but noticeable
	private static final long EARLY_RETURN_TIME_MILLIS = 1500 // should be between ASYNC_DELAY_MILLIS and BLOCK_TIMEOUT_SEC
	private static final long BLOCK_TIMEOUT_SEC = 3 // should be large compared to ASYNC_DELAY_MILLIS

	def setup() {
		msg = Mock(StreamMessage)
		sub = Mock(Subscription)
		iterator = new IterableMessageHandler(5, BLOCK_TIMEOUT_SEC, BLOCK_TIMEOUT_SEC)
	}

	def "hasNext() returns true if there are more messages"() {
		iterator.onMessage(sub, msg)

		expect:
		iterator.hasNext()
	}

	def "hasNext() returns true if the resend is done but there are still queued messages"() {
		iterator.onMessage(sub, msg)
		iterator.done(sub)

		expect:
		iterator.hasNext()
	}

	def "hasNext() returns false if the all messages are processed and the message stream is done"() {
		iterator.done(sub)

		expect:
		!iterator.hasNext()
	}

	def "next() returns received messages"() {
		iterator.onMessage(sub, msg)
		StreamMessage msg2 = Mock(StreamMessage)
		iterator.onMessage(sub, msg2)

		expect:
		iterator.hasNext()
		iterator.next().is(msg)
		iterator.next().is(msg2)
	}

	def "next() throws if there are no more messages"() {
		iterator.done(sub)
		when:
		iterator.next()
		then:
		thrown(NoSuchElementException)
	}

	def "onMessage() blocks when queue is full but returns soon after it's no longer full"() {
		(1..5).forEach {iterator.onMessage(sub, msg)}

		long startTime = System.currentTimeMillis()
		Thread.start {
			Thread.sleep(ASYNC_DELAY_MILLIS)
			iterator.next()
		}
		when:
		iterator.onMessage(sub, msg)
		long elapsedTime = System.currentTimeMillis() - startTime
		then:
		elapsedTime >= ASYNC_DELAY_MILLIS
		elapsedTime < EARLY_RETURN_TIME_MILLIS
	}

	def "onMessage() does not throw on timeout when queue is full"() {
		iterator = new IterableMessageHandler(5, 1, 1)
		(1..5).forEach {iterator.onMessage(sub, msg)}

		long startTime = System.currentTimeMillis()
		when:
		iterator.onMessage(sub, msg)
		then:
		notThrown(RuntimeException)
		System.currentTimeMillis() - startTime >= 1000
	}

	def "hasNext() throws on timeout when queue is empty"() {
		iterator = new IterableMessageHandler(5, 1, 1)
		long startTime = System.currentTimeMillis()
		when:
		iterator.hasNext()
		then:
		thrown(RuntimeException)
		System.currentTimeMillis() - startTime >= 1000
	}

	def "next() throws on timeout when queue is empty"() {
		iterator = new IterableMessageHandler(5, 1, 1)
		long startTime = System.currentTimeMillis()
		when:
		iterator.next()
		then:
		thrown(RuntimeException)
		System.currentTimeMillis() - startTime >= 1000
	}

	def "hasNext() blocks until there are messages but returns soon after"() {
		long startTime = System.currentTimeMillis()
		Thread.start {
			Thread.sleep(ASYNC_DELAY_MILLIS)
			iterator.onMessage(sub, msg)
		}
		when:
		boolean hasNext = iterator.hasNext()
		long elapsedTime = System.currentTimeMillis() - startTime
		then:
		hasNext
		elapsedTime >= ASYNC_DELAY_MILLIS
		elapsedTime < EARLY_RETURN_TIME_MILLIS
	}

	def "next() blocks until there are messages but returns soon after"() {
		long startTime = System.currentTimeMillis()
		Thread.start {
			Thread.sleep(ASYNC_DELAY_MILLIS)
			iterator.onMessage(sub, msg)
		}
		when:
		StreamMessage next = iterator.next()
		long elapsedTime = System.currentTimeMillis() - startTime
		then:
		next.is(msg)
		elapsedTime >= ASYNC_DELAY_MILLIS
		elapsedTime < EARLY_RETURN_TIME_MILLIS
	}

	def "hasNext() blocks until done but returns soon after"() {
		long startTime = System.currentTimeMillis()
		Thread.start {
			Thread.sleep(ASYNC_DELAY_MILLIS)
			iterator.done(sub)
		}
		when:
		boolean hasNext = iterator.hasNext()
		long elapsedTime = System.currentTimeMillis() - startTime
		then:
		!hasNext
		elapsedTime >= ASYNC_DELAY_MILLIS
		elapsedTime < EARLY_RETURN_TIME_MILLIS
	}

	def "next() blocks until done but returns soon after"() {
		long startTime = System.currentTimeMillis()
		Thread.start {
			Thread.sleep(ASYNC_DELAY_MILLIS)
			iterator.done(sub)
		}
		when:
		iterator.next() // throws, don't add lines after this in the when block

		then:
		thrown(NoSuchElementException)
		System.currentTimeMillis() - startTime >= ASYNC_DELAY_MILLIS
		System.currentTimeMillis() - startTime < EARLY_RETURN_TIME_MILLIS
	}

}
