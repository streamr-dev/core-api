package com.unifina.data


import com.unifina.datasource.DataSource
import com.unifina.datasource.ITimeListener
import com.unifina.domain.security.SecUser
import com.unifina.utils.Globals
import spock.lang.Specification

import java.util.function.Consumer

class HistoricalEventQueueSpec extends Specification {

	private HistoricalEventQueue createQueue(Date beginDate, Date endDate, int speed = 0, int capacity = 100) {
		SecUser user = new SecUser()

		Globals globals = new Globals([
			speed: speed,
			beginDate: beginDate.getTime(),
			endDate: endDate.getTime()
		], user, Globals.Mode.HISTORICAL, Mock(DataSource))

		return new HistoricalEventQueue(globals, globals.getDataSource(), capacity)
	}

	void "reports all seconds between start time and end time"() {
		HistoricalEventQueue queue = createQueue(
			new Date(1552521600000L), // Thursday, March 14, 2019 00:00:00 UTC
			new Date(1552525200000L)  // Thursday, March 14, 2019 01:00:00 UTC
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

		int eventsProcessed = 0

		// Fill up the queue with events around halfway of the playback period
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
		queue.start() // If there's a problem, this may get deadlocked

		then:
		eventsProcessed == i
	}

	void "on event, adding more events to a full queue won't deadlock the system"() {
		HistoricalEventQueue queue = createQueue(
			new Date(0),
			new Date(200 * 1000)
		)

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
		queue.start() // If there's a problem, this may get deadlocked

		then:
		eventsProcessed == i
		extraEventsProcessed == 100
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

}
