package com.unifina.task

import com.unifina.BeanMockingSpecification
import com.unifina.domain.Stream
import com.unifina.domain.User
import com.unifina.domain.Canvas
import com.unifina.domain.Task
import com.unifina.service.CanvasService
import grails.test.mixin.Mock

/**
 * For some reason this test throws in cleanupSpec due to running
 * GrailsUnitTestMixin.shutdownApplicationContext multiple times.
 * Probably related to buggy Mixins.
 */
@Mock([Canvas, Stream, Task])
class CanvasDeleteTaskSpec extends BeanMockingSpecification {

	CanvasService canvasService
	Canvas canvas
    User user

	def setup() {
		canvasService = mockBean(CanvasService, Mock(CanvasService))

		canvas = new Canvas().save(validate: false)
		assert canvas.id != null

		user = new User()
	}

	void "CanvasDeleteTask must call canvasService.deleteCanvas()"() {
		CanvasDeleteTask task = new CanvasDeleteTask(
				new Task(user: user).save(validate: false),
				CanvasDeleteTask.getConfig(canvas),
				null)

		when:
		task.run()

		then:
		1 * canvasService.deleteCanvas(canvas, user, false)
	}

}
