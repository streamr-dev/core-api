package com.unifina.controller.core.signalpath

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.UiChannel
import com.unifina.service.SerializationService
import com.unifina.service.SignalPathService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(LiveController)
@Mock([SecUser, Canvas, Module, UiChannel])
class LiveControllerSpec extends Specification {
	void setup() {
		defineBeans {
			serializationService(SerializationService)
			signalPathService(SignalPathService) { it.autowire = true }
		}
		controller.signalPathService = mainContext.getBean(SignalPathService)
	}

	void "deserialization failure results in error message"() {
		setup: "canvas has been incorrectly serialized to database"
		def canvas = new Canvas(
			name: "name",
			adhoc: false,
			serialized: "{invalid: 'serialization'}"
		)
		canvas.save(failOnError: true, validate: false)

		when: "runningSignalPath is started"
		params.id = canvas.id
		controller.start()

		then: "error flash message is shown"
		flash.error.length() > 0
	}
}