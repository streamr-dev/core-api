package com.unifina.signalpath

import com.unifina.BeanMockingSpecification
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.service.ModuleService
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

@TestMixin(GrailsUnitTestMixin)
class SignalPathSpec extends BeanMockingSpecification {

	def setup() {
		mockBean(ModuleService, Mock(ModuleService))
	}

	def "Runtime path is correctly formed"() {
		Canvas topCanvas = new Canvas()
		topCanvas.id = "canvasId"
		Globals globals = GlobalsFactory.createInstance([:], grailsApplication, new SecUser())

		SignalPath topSignalPath = new SignalPath(true)
		topSignalPath.setCanvas(topCanvas)

		SignalPath subSignalPath = new SignalPath(false)
		subSignalPath.setParentSignalPath(topSignalPath)
		subSignalPath.setHash(5)
		AbstractSignalPathModule module = new AbstractSignalPathModule() {
			@Override
			void sendOutput() {}
			@Override
			void clearState() {}
		}
		module.setParentSignalPath(subSignalPath)
		module.setHash(10)

		expect:
		subSignalPath.getRuntimePath() == "/canvases/canvasId/modules/5"
		module.getRuntimePath() == "/canvases/canvasId/modules/5/modules/10"
	}

	def "getRootSignalPath()"() {
		SignalPath signalPath

		when: "this is root"
		signalPath = new SignalPath(true)
		then:
		signalPath.getRootSignalPath() == signalPath


		when: "has parent"
		SignalPath top = new SignalPath(true)
		signalPath = new SignalPath(false)
		signalPath.setParentSignalPath(top)
		then:
		signalPath.getRootSignalPath() == top
	}

}
