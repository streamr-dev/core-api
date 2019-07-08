package com.unifina.data


import com.unifina.datasource.DataSource
import com.unifina.datasource.ITimeListener
import com.unifina.domain.security.SecUser
import com.unifina.utils.Globals
import org.apache.log4j.Logger
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

class HistoricalEventQueueSpec extends Specification {

	private static final Logger log = Logger.getLogger(HistoricalEventQueueSpec)

	private HistoricalEventQueue createQueue(Date beginDate, Date endDate, int speed = 0, int capacity = 100, boolean addEndEvent = true) {
		SecUser user = new SecUser()

		Globals globals = new Globals([
			speed: speed,
			beginDate: beginDate.getTime(),
			endDate: endDate.getTime()
		], user, Globals.Mode.HISTORICAL, Mock(DataSource))

		HistoricalEventQueue queue = new HistoricalEventQueue(globals, globals.getDataSource(), capacity)

		// First event
		queue.enqueue(new Event(null, beginDate, null))

		if (addEndEvent) {
			// Abort queue after this last event has been processed
			queue.enqueue(new Event(null, endDate, new Consumer() {
				@Override
				void accept(Object o) {
					queue.abort()
				}
			}))
		}

		return queue
	}

	/**
	 * Fills up the queue with events that increment the given eventCounter.
	 * Returns the number of events added to the queue.
	 */
	private static int fillUpQueueWithCountingEvents(HistoricalEventQueue queue, AtomicInteger eventCounter, Date startDate, long eventIntervalMillis) {
		int result = queue.remainingCapacity()
		for (int i=1; i<=result; i++) {
			queue.enqueue(new Event<Integer>(i, new Date(startDate.getTime() + (i * eventIntervalMillis)), new Consumer<Integer>() {
				@Override
				void accept(Integer integer) {
					eventCounter.incrementAndGet()
				}
			}))
		}
		assert queue.isFull()

		return result
	}

	void "reports all seconds between queued events"() {
		HistoricalEventQueue queue = createQueue(
			new Date(0),
			new Date(60 * 60 * 1000)
		)

		int tickCounter = 0
		queue.addTimeListener(new ITimeListener() {
			@Override
			void setTime(Date time) {
				tickCounter++
			}
			@Override
			int tickRateInSec() {
				return 1
			}
		})

		when:
		queue.start()

		then:
		tickCounter == 60*60
	}

	void "reports time events correctly when queue is full"() {
		HistoricalEventQueue queue = createQueue(
			new Date(0),
			new Date(200 * 1000)
		)

		AtomicInteger eventsProcessed = new AtomicInteger(0)

		// Fill up the queue with events around halfway of the playback period
		int eventsAdded = fillUpQueueWithCountingEvents(queue, eventsProcessed, new Date(100 * 1000), 1)

		when:
		queue.start() // If there's a problem, this may get deadlocked

		then:
		eventsProcessed.get() == eventsAdded
	}

	void "on event, adding more events to a full queue won't deadlock the system"() {
		HistoricalEventQueue queue = createQueue(
			new Date(0),
			new Date(200 * 1000)
		)

		int extraEventsProcessed = 0

		// Add an event that adds more events
		queue.enqueue(new Event<Integer>(0, new Date(0), new Consumer<Integer>() {
			@Override
			void accept(Integer integer) {
				for (int i=0; i<100; i++) {
					log.info("Trying to enqueue extra event $i")
					queue.enqueue(new Event<Integer>(i, new Date(0), new Consumer<Integer>() {
						@Override
						void accept(Integer ii) {
							log.info("Extra event was dispatched: $ii")
							extraEventsProcessed++
						}
					}))
				}
			}
		}))

		AtomicInteger eventsProcessed = new AtomicInteger(0)

		// Fill up the queue with events around halfway of the playback period
		int eventsAdded = fillUpQueueWithCountingEvents(queue, eventsProcessed, new Date(100 * 1000), 1)

		when:
		queue.start() // If there's a problem, this may get deadlocked

		then:
		new PollingConditions().within(20) {
			eventsProcessed.get() == eventsAdded && extraEventsProcessed == 100
		}
	}

	void "observes speed"() {
		HistoricalEventQueue queue = createQueue(
			new Date(1552521600000L), // Thursday, March 14, 2019 00:00:00 UTC
			new Date(1552521603000L), // Thursday, March 14, 2019 00:00:03 UTC
			1
		)
		long startTime = System.currentTimeMillis()

		when:
		queue.start() // This should take 3 seconds to run
		long elapsed = System.currentTimeMillis() - startTime

		then:
		elapsed >= 3000
	}

	void "prioritizes messages based on timestamp"() {
		HistoricalEventQueue queue = createQueue(
			new Date(0L),
			new Date(3000L)
		)
		boolean success = false

		when:
		queue.enqueue(new Event(null, new Date(2000L), new Consumer() {
			@Override
			void accept(Object o) {
				success = (queue.size() == 1) // must still contain the playback end event when this event is processed
			}
		}))
		queue.start()

		then:
		new PollingConditions().within(2) {
			success
		}
	}

	void "won't stop processing before aborted, but should stop soon after aborting"() {
		HistoricalEventQueue queue = createQueue(
			new Date(0L),
			new Date(3000L),
			0, 100, false // don't add end event
		)

		when:
		long startTime = System.currentTimeMillis()
		// Abort in 2 sec
		Thread.start {
			Thread.sleep(2000)
			queue.abort()
		}
		queue.start()
		long endTime = System.currentTimeMillis()

		then:
		endTime - startTime > 2000
		endTime - startTime < 5000
	}

}
