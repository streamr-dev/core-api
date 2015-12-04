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
			signalPathService(SignalPathService) { it.autowire = true }
		}
		controller.signalPathService = mainContext.getBean(SignalPathService)
	}

	void "deserialization failure results in error message"() {
		setup: "runningSignalPath has been incorrectly serialized to database"
		def rsp = new RunningSignalPath(
			name: "name",
			adhoc: true,
			serialized: "{invalid: 'serialization'}"
		)
		rsp.save(failOnError: true, validate: false)

		when: "runningSignalPath is started"
		params.id = rsp.id
		controller.start()

		then: "error flash message is shown"
		flash.error.length() > 0
	}
}