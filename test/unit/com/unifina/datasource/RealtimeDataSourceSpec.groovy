package com.unifina.datasource

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.service.SerializationService
import com.unifina.service.SignalPathService
import com.unifina.signalpath.SignalPath
import com.unifina.signalpath.SignalPathRunner
import com.unifina.utils.Globals
import grails.test.mixin.Mock
import grails.util.Holders
import spock.lang.Specification

@Mock([SecUser, Canvas])
class RealtimeDataSourceSpec extends Specification {
	SerializationService serializationService
	SignalPathService signalPathService

	void setup() {
		serializationService = Mock(SerializationService)
		Holders.getApplicationContext().beanFactory.registerSingleton("serializationService", serializationService)
		signalPathService = Mock(SignalPathService)
		Holders.getApplicationContext().beanFactory.registerSingleton("signalPathService", signalPathService)
	}

	void cleanup() {
		Holders.getApplicationContext().beanFactory.destroySingleton("serializationService")
		Holders.getApplicationContext().beanFactory.destroySingleton("signalPathService")
	}

	void "when SignalPathRunner thread is killed dont mark canvas to stopped state"() {
		setup:
		Globals globals = new Globals([:], new SecUser(), Globals.Mode.REALTIME, null)
		SignalPath sp = new SignalPath()
		Canvas c = new Canvas()
		c.state = Canvas.State.RUNNING
		c.id = "canvas-id-1"
		c.save()
		sp.setCanvas(c)
		SignalPathRunner runner = new SignalPathRunner(sp, globals)
		runner.start()
		runner.waitRunning(true)
		when:
		runner.interrupt()
		then:
		1 * serializationService.serializationIntervalInMillis() >> 0L
		c.state == Canvas.State.RUNNING
	}
}
