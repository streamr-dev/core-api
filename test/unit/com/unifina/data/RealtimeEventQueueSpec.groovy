package com.unifina.data

import com.unifina.datasource.DataSource
import com.unifina.domain.security.SecUser
import com.unifina.utils.Globals
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.function.Consumer

class RealtimeEventQueueSpec extends Specification {

	private RealtimeEventQueue createQueue(int capacity = 100) {
		SecUser user = new SecUser()

		Globals globals = new Globals([:], user, Globals.Mode.REALTIME, Mock(DataSource))

		return new RealtimeEventQueue(globals, globals.getDataSource(), capacity)
	}

	void "basic queue operation"() {
		RealtimeEventQueue queue = createQueue()
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

	void "on event, adding more events to a full queue won't deadlock the system"() {
		RealtimeEventQueue queue = createQueue()

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

}
