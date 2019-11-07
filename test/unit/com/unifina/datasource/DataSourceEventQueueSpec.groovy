package com.unifina.datasource

import com.unifina.data.Event
import com.unifina.domain.security.SecUser
import com.unifina.utils.Globals
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.function.Consumer

class DataSourceEventQueueSpec extends Specification {

	private DataSourceEventQueue createQueue(int capacity = 100) {
		SecUser user = new SecUser()

		Globals globals = new Globals([:], user, Globals.Mode.REALTIME, Mock(DataSource))

		return new DataSourceEventQueue(globals, globals.getDataSource(), capacity, false)
	}

	void "basic queue operation"() {
		DataSourceEventQueue queue = createQueue()
		int eventsProcessed = 0

		when:
		Thread consumerThread = Thread.start {
			queue.start()
		}

		Thread producerThread = Thread.start {
			// Enqueue events
			for (int i=0; i<1000; i++) {
				queue.enqueue(new Event<Integer>(i, new Date(i * 1000), new Consumer<Integer>() {
					@Override
					void accept(Integer integer) {
						eventsProcessed++
					}
				}))
			}
		}

		then:
		new PollingConditions().within(10, {
			eventsProcessed == 1000
			!producerThread.isAlive()
		})

		when:
		queue.abort()

		then:
		new PollingConditions().within(5, {
			!consumerThread.isAlive()
		})
	}

	void "if the queue is aborted before it is started, it should abort immediately after starting"() {
		DataSourceEventQueue queue = createQueue()
		int eventsProcessed = 0
		boolean stopProducer = false

		when:
		queue.abort()
		Thread consumerThread = Thread.start {
			queue.start()
		}

		Thread producerThread = Thread.start {
			// Enqueue events
			for (int i=0; i<1000 && !stopProducer; i++) {
				queue.enqueue(new Event<Integer>(i, new Date(i * 1000), new Consumer<Integer>() {
					@Override
					void accept(Integer integer) {
						eventsProcessed++
					}
				}))
			}
		}

		then:
		new PollingConditions().within(2, {
			!consumerThread.isAlive()
			eventsProcessed < 1000
		})

		when:
		stopProducer = true

		then:
		new PollingConditions().within(2, {
			!producerThread.isAlive()
		})
	}

	void "on event, adding more events to a full queue won't deadlock the system"() {
		DataSourceEventQueue queue = createQueue()

		int eventsProcessed = 0
		int extraEventsProcessed = 0

		// Add an event that adds more events
		queue.enqueue(new Event<Integer>(0, new Date(0), new Consumer<Integer>() {
			@Override
			void accept(Integer integer) {
				for (int i=0; i<100; i++) {
					queue.enqueue(new Event<Integer>(null, new Date(0), new Consumer<Integer>() {
						@Override
						void accept(Integer ii) {
							extraEventsProcessed++
						}
					}))
				}
			}
		}))

		// Fill up the queue
		int i = 0
		while (queue.remainingCapacity() > 0) {
			queue.enqueue(new Event<Integer>(i, new Date(100*1000 + i), new Consumer<Integer>() {
				@Override
				void accept(Integer integer) {
					eventsProcessed++
				}
			}))
			i++
		}
		assert queue.isFull()

		when:
		Thread consumerThread = Thread.start {
			queue.start()
		}

		then:
		new PollingConditions().within(10, {
			eventsProcessed == i
			extraEventsProcessed == 100
		})

		when:
		queue.abort()

		then:
		new PollingConditions().within(5, {
			!consumerThread.isAlive()
		})
	}

	void "every full second between subsequent event timestamps is being reported"() {
		setup:
		DataSourceEventQueue queue = createQueue()
		ITimeListener timeListener = Mock(ITimeListener)
		queue.addTimeListener(timeListener)
		int eventsProcessed = 0
		11 * timeListener.tickRateInSec() >> 1
		1 * timeListener.setTime(new Date(10000))
		1 * timeListener.setTime(new Date(11000))
		1 * timeListener.setTime(new Date(12000))
		1 * timeListener.setTime(new Date(13000))
		1 * timeListener.setTime(new Date(14000))
		1 * timeListener.setTime(new Date(15000))
		1 * timeListener.setTime(new Date(16000))
		1 * timeListener.setTime(new Date(17000))
		1 * timeListener.setTime(new Date(18000))
		1 * timeListener.setTime(new Date(19000))
		1 * timeListener.setTime(new Date(20000))

		when:
		Thread consumerThread = Thread.start {
			queue.start()
		}

		Thread producerThread = Thread.start {
			queue.enqueue(new Event<Integer>(1, new Date(10000), new Consumer<Integer>() {
				@Override
				void accept(Integer integer) {
					eventsProcessed++
				}
			}))
			queue.enqueue(new Event<Integer>(2, new Date(20100), new Consumer<Integer>() {
				@Override
				void accept(Integer integer) {
					eventsProcessed++
				}
			}))
		}

		then:
		new PollingConditions().within(10, {
			eventsProcessed == 2 && !producerThread.isAlive()
		})

		when:
		queue.abort()

		then:
		new PollingConditions().within(5, {
			!consumerThread.isAlive()
		})
	}
}
