package com.unifina.controller.core.signalpath

import com.unifina.domain.signalpath.RunningSignalPath
import com.unifina.service.SerializationService
import com.unifina.service.SignalPathService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(LiveController)
@Mock(RunningSignalPath)
class LiveControllerSpec extends Specification {

	void setup() {
		defineBeans {
			serializationService(SerializationService)
			signalPathService(SignalPathService)
		}

		// Manual wiring required
		def signalPathService = mainContext.getBean("signalPathService")
		signalPathService.grailsApplication = grailsApplication
		signalPathService.serializationService = mainContext.getBean("serializationService")
	}

	void "deserialization failure results in error message"() {
		setup: "runningSignalPath has been incorrectly serialized"
		def rsp = new RunningSignalPath()
		rsp.name = "name"
		rsp.adhoc = true
		rsp.serialized = "{hello: 'world'}"
		rsp.save(failOnError: true, validate: false)

		when: "runningSignalPath is started"
		params.id = rsp.id
		controller.start()

		then: "error flash message is shown"
		flash.error.length() > 0
	}
}