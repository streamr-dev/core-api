package com.unifina.data

import com.unifina.datasource.DataSource
import com.unifina.datasource.ITimeListener
import com.unifina.domain.security.SecUser
import com.unifina.utils.Globals
import spock.lang.Specification

class HistoricalEventQueueSpec extends Specification {

	private HistoricalEventQueue createQueue(Date beginDate, Date endDate, speed = 0) {
		SecUser user = new SecUser()

		Globals globals = new Globals([
			speed: speed,
			beginDate: beginDate.getTime(),
			endDate: endDate.getTime()
		], user, Globals.Mode.HISTORICAL, Mock(DataSource))

		return new HistoricalEventQueue(globals, globals.getDataSource())
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
