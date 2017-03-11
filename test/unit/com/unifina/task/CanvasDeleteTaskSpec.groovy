package com.unifina.task

import com.unifina.BeanMockingSpec
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.task.Task
import com.unifina.service.CanvasService
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
@Mock([Canvas, Stream])
class CanvasDeleteTaskSpec extends BeanMockingSpec {

	CanvasService canvasService
	Canvas canvas
	SecUser user
	List<Stream> uiChannels

    def setup() {
		canvasService = mockBean(CanvasService, Mock(CanvasService))

		canvas = new Canvas().save(validate: false)
		assert canvas.id != null

		uiChannels = (1..3).collect {
			Stream s = new Stream()
			s.id = it.toString()
			s.name = it.toString()
			s.uiChannel = true
			s.uiChannelCanvas = canvas
			s.save(validate: false)
			return s
		}
		uiChannels.each {
			assert it.id != null
		}
		user = new SecUser()
    }

	def cleanup() {
		cleanupMockBeans()
	}

	void "CanvasDeleteTask must call canvasService.deleteCanvas()"() {
		CanvasDeleteTask task = new CanvasDeleteTask(
				new Task(user: user),
				CanvasDeleteTask.getConfig(canvas, uiChannels),
				grailsApplication)

		when:
		task.run()

		then:
		1 * canvasService.deleteCanvas(canvas, user, false, uiChannels)
	}

}
