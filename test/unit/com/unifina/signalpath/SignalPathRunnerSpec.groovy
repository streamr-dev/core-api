package com.unifina.signalpath

import com.unifina.BeanMockingSpecification
import com.unifina.datasource.DataSource
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.service.SignalPathService
import com.unifina.utils.Globals
import spock.util.concurrent.PollingConditions

class SignalPathRunnerSpec extends BeanMockingSpecification {

	SignalPathService signalPathService
	SignalPathRunner runner
	DataSource dataSource
	SignalPath sp
	Canvas canvas
	boolean dataSourceStarted

	def setup() {
		dataSourceStarted = false
		dataSource = Mock(DataSource)
		dataSource.start() >> {
			dataSourceStarted = true
			// Block until interrupted by the test
			try {
				Thread.sleep(30 * 1000)
			} catch (err) {
				// ignore
			}
		}

		sp = new SignalPath()
		canvas = new Canvas()
		canvas.state = Canvas.State.RUNNING
		sp.setCanvas(canvas)

		signalPathService = mockBean(SignalPathService, Mock(SignalPathService))
		signalPathService.updateState(_, _) >> { String runnerId, Canvas.State state ->
			canvas.state = state
		}
	}

	void "when SignalPathRunner thread is killed, dont mark realtime canvas to stopped state"() {
		Globals globals = new Globals([:], new SecUser(), Globals.Mode.REALTIME, dataSource)
		canvas.adhoc = false
		runner = new SignalPathRunner(sp, globals)

		when:
		runner.start()

		then:
		new PollingConditions().within(5) {
			dataSourceStarted == true
		}
		runner.isAlive()

		when:
		runner.interrupt()

		then:
		new PollingConditions().within(5) {
			!runner.isAlive()
		}
		canvas.state == Canvas.State.RUNNING
	}

	void "when SignalPathRunner thread is killed, mark historical canvas to stopped state"() {
		Globals globals = new Globals([beginDate: new Date().getTime(), endDate: new Date().getTime()], new SecUser(),
			Globals.Mode.HISTORICAL, dataSource)
		canvas.adhoc = true
		runner = new SignalPathRunner(sp, globals)

		when:
		runner.start()

		then:
		new PollingConditions().within(5) {
			dataSourceStarted == true
		}
		runner.isAlive()

		when:
		runner.interrupt()

		then:
		new PollingConditions().within(5) {
			!runner.isAlive()
		}
		canvas.state == Canvas.State.STOPPED
	}
}
