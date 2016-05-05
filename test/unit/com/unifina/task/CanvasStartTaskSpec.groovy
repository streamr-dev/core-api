package com.unifina.task

import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.task.Task
import com.unifina.service.CanvasService
import grails.test.mixin.Mock
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@Mixin(GrailsUnitTestMixin) // for grailsApplication
@Mock([Canvas])
class CanvasStartTaskSpec extends Specification {

	Canvas canvas
	CanvasService canvasService

    def setup() {
		canvasService = Mock(CanvasService)
		canvas = new Canvas(state: "stopped")
		canvas.save(validate:false)
    }

    def cleanup() {

    }

	void "task must start canvas"() {
		CanvasStartTask task = new CanvasStartTask(
				new Task(),
				CanvasStartTask.getConfig(canvas, false, false),
				grailsApplication,
				canvasService)

		when:
		task.run()

		then:
		1 * canvasService.start(canvas, false)
		0 * canvasService.start(canvas, true)
	}

	void "task must retry starting with reset-and-start if resetOnFail==true"() {
		CanvasStartTask task = new CanvasStartTask(
				new Task(),
				CanvasStartTask.getConfig(canvas, false, true),
				grailsApplication,
				canvasService)

		when:
		task.run()

		then:
		1 * canvasService.start(canvas, false) >> { throw new Exception("Mocked exception") }
		and:
		1 * canvasService.start(canvas, true)
	}

	void "task must reset-and-start canvas if forceReset==true"() {
		CanvasStartTask task = new CanvasStartTask(
				new Task(),
				CanvasStartTask.getConfig(canvas, true, false),
				grailsApplication,
				canvasService)

		when:
		task.run()

		then:
		1 * canvasService.start(canvas, true)
		0 * canvasService.start(canvas, false)
	}

	void "must not retry reset-and-starting if forceReset==true and resetOnFail==true"() {
		CanvasStartTask task = new CanvasStartTask(
				new Task(),
				CanvasStartTask.getConfig(canvas, true, true),
				grailsApplication,
				canvasService)

		when:
		task.run()

		then:
		1 * canvasService.start(canvas, true)
		0 * canvasService.start(canvas, false)
	}

}
