package com.unifina.task


import com.unifina.domain.User
import com.unifina.domain.Canvas
import com.unifina.domain.Task
import com.unifina.service.CanvasService
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock([Canvas, User, Task])
class CanvasStartTaskSpec extends Specification {

	Canvas canvas
	CanvasService canvasService
	User user

    def setup() {
		canvasService = Mock(CanvasService)
		canvas = new Canvas(state: "stopped")
		canvas.save(validate: false)
		user = new User(username: "user@streamr.com")
		user.save(validate: false, failOnError: true)
    }

    def cleanup() {

    }

	void "task must start canvas"() {
		CanvasStartTask task = new CanvasStartTask(
				new Task(user: user),
				CanvasStartTask.getConfig(canvas, false, false),
				grailsApplication,
				canvasService)

		when:
		task.run()

		then:
		1 * canvasService.start(canvas, false, user)
		0 * canvasService.start(canvas, true, user)
	}

	void "task must retry starting with reset-and-start if resetOnFail==true"() {
		CanvasStartTask task = new CanvasStartTask(
				new Task(user: user),
				CanvasStartTask.getConfig(canvas, false, true),
				grailsApplication,
				canvasService)

		when:
		task.run()

		then:
		1 * canvasService.start(canvas, false, user) >> { throw new Exception("Mocked exception") }
		and:
		1 * canvasService.start(canvas, true, user)
	}

	void "task must reset-and-start canvas if forceReset==true"() {
		CanvasStartTask task = new CanvasStartTask(
				new Task(user: user),
				CanvasStartTask.getConfig(canvas, true, false),
				grailsApplication,
				canvasService)

		when:
		task.run()

		then:
		1 * canvasService.start(canvas, true, user)
		0 * canvasService.start(canvas, false, user)
	}

	void "must not retry reset-and-starting if forceReset==true and resetOnFail==true"() {
		CanvasStartTask task = new CanvasStartTask(
				new Task(user: user),
				CanvasStartTask.getConfig(canvas, true, true),
				grailsApplication,
				canvasService)

		when:
		task.run()

		then:
		1 * canvasService.start(canvas, true, user)
		0 * canvasService.start(canvas, false, user)
	}

}
