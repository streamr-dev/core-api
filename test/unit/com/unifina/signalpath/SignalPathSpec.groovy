package com.unifina.signalpath

import com.unifina.BeanMockingSpecification
import com.unifina.domain.signalpath.Canvas
import com.unifina.exceptions.CyclicCanvasModuleExceptionMessage
import com.unifina.service.ModuleService
import grails.test.mixin.Mock

@Mock(Canvas)
class SignalPathSpec extends BeanMockingSpecification {

	def setup() {
		mockBean(ModuleService, Mock(ModuleService))
	}

	def "Runtime path is correctly formed"() {
		Canvas topCanvas = new Canvas()
		topCanvas.id = "canvasId"

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

	def "cyclic dependency detection"() {
		Canvas rootCanvas = new Canvas()
		rootCanvas.id = "root"
		rootCanvas.save(validate: false)

		SignalPath rootSignalPath = new SignalPath(true)
		rootSignalPath.init()
		rootSignalPath.configure([canvasId: rootCanvas.id])

		SignalPath subSignalPath = new SignalPath(false)
		subSignalPath.init()
		subSignalPath.setParentSignalPath(rootSignalPath)

		when:
		subSignalPath.configure([
			hash: 123,
			params: [
		    	[name: "canvas", value: "root"]
			]
		])

		then:
		ModuleException ex = thrown()

		ex.getModuleExceptions()[0] instanceof CyclicCanvasModuleExceptionMessage
		ex.getModuleExceptions()[0].getModuleId() == 123
	}

}
