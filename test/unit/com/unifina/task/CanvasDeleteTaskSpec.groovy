package com.unifina.task

import com.unifina.BeanMockingSpecification
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.task.Task
import com.unifina.service.CanvasService
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.spring.GrailsApplicationContext
import org.springframework.context.ApplicationContext

@Mock([Canvas, Stream, Task])
class CanvasDeleteTaskSpec extends BeanMockingSpecification {

	CanvasService canvasService
	Canvas canvas
	SecUser user

    def setup() {
		canvasService = mockBean(CanvasService, Mock(CanvasService))

		canvas = new Canvas().save(validate: false)
		assert canvas.id != null

		user = new SecUser()
    }

	def cleanup() {
		cleanupMockBeans()
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
