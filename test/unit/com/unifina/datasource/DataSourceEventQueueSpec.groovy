package com.unifina.datasource


import com.unifina.data.Event
import com.unifina.domain.User
import com.unifina.utils.Globals
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.function.Consumer

class DataSourceEventQueueSpec extends Specification {

	private DataSourceEventQueue createQueue(int capacity = 100) {
		User user = new User()

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
		long baseTime = queue.globals.getTime().getTime()
		long firstExpectedSec = baseTime - (baseTime % 1000) + 1000
		List<Long> reportedTimes = []

		queue.addTimeListener(new ITimeListener() {
			@Override
			void setTime(Date time) {
				reportedTimes.push(time.getTime())
			}

			@Override
			int tickRateInSec() {
				return 1
			}
		})
		int eventsProcessed = 0

		when:
		Thread consumerThread = Thread.start {
			queue.start()
		}

		Thread producerThread = Thread.start {
			queue.enqueue(new Event<Integer>(1, new Date(firstExpectedSec + 10000), new Consumer<Integer>() {
				@Override
				void accept(Integer integer) {
					eventsProcessed++
				}
			}))
			queue.enqueue(new Event<Integer>(2, new Date(firstExpectedSec + 20100), new Consumer<Integer>() {
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
		reportedTimes == (firstExpectedSec..(firstExpectedSec + 20000)).step(1000)

		when:
		queue.abort()

		then:
		new PollingConditions().within(5, {
			!consumerThread.isAlive()
		})
	}

	void "first reported time tick can't be less than the initial globals.time"() {
		DataSourceEventQueue queue = createQueue()
		Long minTime = queue.globals.time.getTime() // initialized to current system time when the Globals object is created
		List<Long> reportedTimes = []

		when:
		queue.addTimeListener(new ITimeListener() {
			@Override
			void setTime(Date time) {
				reportedTimes.add(time.getTime())
			}

			@Override
			int tickRateInSec() {
				return 1
			}
		})

		queue.handleEvent(new Event<Integer>(0, new Date(minTime - 1000), 0, null))
		queue.handleEvent(new Event<Integer>(0, new Date(minTime), 0, null))
		queue.handleEvent(new Event<Integer>(0, new Date(minTime + 1000), 0, null))

		then:
		!reportedTimes.isEmpty()
		reportedTimes.count { it < minTime } == 0
	}
}
