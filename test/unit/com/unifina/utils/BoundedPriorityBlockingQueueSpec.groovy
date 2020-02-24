package com.unifina.utils

import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

class BoundedPriorityBlockingQueueSpec extends Specification {

	BlockingQueue<Number> queue

	def setup() {
		queue = new BoundedPriorityBlockingQueue<>(5)
	}

	def "remainingCapacity()"() {
		expect:
		queue.remainingCapacity() == 5

		when:
		(1..5).forEach { queue.offer(it, 1, TimeUnit.SECONDS) }

		then:
		queue.remainingCapacity() == 0

		when:
		queue.poll(1, TimeUnit.SECONDS)

		then:
		queue.remainingCapacity() == 1
	}

	def "the queue blocks on offer when it is full"() {
		(1..5).forEach { queue.offer(it, 1, TimeUnit.SECONDS) }
		long startTime = System.currentTimeMillis()

		when:
		boolean result = queue.offer(0, 1, TimeUnit.SECONDS)

		then:
		!result
		// At least 1 sec must have elapsed
		System.currentTimeMillis() - startTime >= 1000
	}

	def "the queue does not block on offer when it is not full"() {
		long startTime = System.currentTimeMillis()

		when:
		List trueResults = (1..5).findAll { queue.offer(it, 1, TimeUnit.SECONDS) }

		then:
		trueResults.size() == 5
		// Less than 1 sec must have elapsed
		System.currentTimeMillis() - startTime < 1000
	}

	def "the queue blocks on poll when it is empty"() {
		long startTime = System.currentTimeMillis()

		when:
		boolean result = queue.poll(1, TimeUnit.SECONDS)

		then:
		!result
		// At least 1 sec must have elapsed
		System.currentTimeMillis() - startTime >= 1000
	}

	def "the queue does not block on poll when it has items"() {
		long startTime = System.currentTimeMillis()
		(1..5).forEach { queue.offer(it, 1, TimeUnit.SECONDS) }

		when:
		(1..5).forEach { queue.poll(1, TimeUnit.SECONDS) }

		then:
		// Less than 1 sec must have elapsed
		System.currentTimeMillis() - startTime < 1000
	}

	def "the queue works as expected with simultaneous production and consumption"() {
		boolean stop = false
		int produced = 0
		int consumed = 0

		when:
		Thread producer = Thread.start {
			while (!stop) {
				if (queue.offer(Math.random(), 1, TimeUnit.SECONDS)) {
					produced++
				} else {
					throw new RuntimeException("offer timed out!")
				}

				if (queue.size() > 5) {
					throw new RuntimeException("queue capacity violated!")
				}
			}
		}
		Thread consumer = Thread.start {
			while (producer.isAlive() || !queue.isEmpty()) { // finish processing the queue even if stop is signaled
				if (queue.poll(1, TimeUnit.SECONDS)) {
					consumed++
				}

				if (queue.size() > 5) {
					throw new RuntimeException("queue capacity violated!")
				}
			}
		}

		Thread.sleep(1000)
		stop = true

		then:
		new PollingConditions().within(2) {
			!consumer.isAlive() && !producer.isAlive()
		}
		produced > 0
		consumed > 0
		produced == consumed
	}
}
