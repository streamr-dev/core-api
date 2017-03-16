package com.unifina.signalpath.map

import com.unifina.api.SaveCanvasCommand
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.Module
import com.unifina.service.CanvasService
import com.unifina.service.ModuleService
import com.unifina.service.PermissionService
import com.unifina.service.SignalPathService
import com.unifina.signalpath.RuntimeRequest
import com.unifina.signalpath.SignalPath
import com.unifina.signalpath.simplemath.Divide
import com.unifina.signalpath.simplemath.Sum
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory
import com.unifina.utils.testutils.ModuleTestHelper
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import spock.lang.Specification

@TestMixin(ControllerUnitTestMixin)
@Mock([Canvas, Module, SecUser, ModuleService, SpringSecurityService, SignalPathService, CanvasService, PermissionService])
class ForEachSpec extends Specification {

	CanvasService canvasService

	Globals globals
	ForEach module
	SecUser user

	def modulesJson

	def setup() {
		canvasService = mainContext.getBean(CanvasService)
		canvasService.signalPathService = mainContext.getBean(SignalPathService)
		module = new ForEach()
		module.globals = globals = GlobalsFactory.createInstance([:], grailsApplication, user)
		module.init()
		user = new SecUser().save(failOnError: true, validate: false)

		def divideModule = new Module(implementingClass: Divide.canonicalName).save(failOnError: true, validate: false)
		def sumModule = new Module(implementingClass: Sum.canonicalName).save(failOnError: true, validate: false)
		modulesJson = [
			[
				id: divideModule.id,
				hash: 0,
				name: "Divide",
				type: "module",
				params: [],
				inputs: [
					[
						connected: false,
						drivingInput: true,
						export: true,
						id: "myId_0_1457000930631",
						longName: "Divide.A",
						name: "A",
						type: "Double"
					],
					[
						connected: false,
						drivingInput: true,
						export: true,
						id: "myId_0_1457000930651",
						longName: "Divide.B",
						name: "B",
						type: "Double"
					]
				],
				outputs: [
					[
						connected: true,
						id: "myId_0_1457000930660",
						longName: "Divide.A/B",
						name: "A/B",
						type: "Double",
						targets: ["myId_1_1457000939281"]
					]
				]
			],
			[
				id: sumModule.id,
				hash: 1,
				name: "Sum",
				type: "module",
				params: [],
				inputs: [
					[
						connected: true,
						drivingInput: true,
						id: "myId_1_1457000939281",
						longName: "Sum.in",
						name: "in",
						sourceId: "myId_0_1457000930660",
						type: "Double"
					]
				],
				outputs: [
					[
						connected: false,
						export: true,
						id: "myId_1_1457000939285",
						longName: "Sum.out",
						name: "out",
						type: "Double",
						targets: ["myId_1_1457000939281"]
					]
				]
			]
		]
	}

	def "throws RuntimeException if canvas has no exported inputs"() {
		def command = new SaveCanvasCommand(name: "canvas-wo-exported-inputs", modules: [])
		def canvas = canvasService.createNew(command, user)

		when:
		module.getInput("canvas").receive(canvas.id)
		module.configure(module.getConfiguration())

		then:
		RuntimeException e = thrown()
		e.message.contains("canvas")
	}

	def "forEach works correctly"() {
		def command = new SaveCanvasCommand(name: "sub-canvas", modules: modulesJson)
		def canvas = canvasService.createNew(command, user)

		module.getInput("canvas").receive(canvas.id)
		module.configure(module.getConfiguration())

		when:
		Map inputValues = [
			key: ["k1", "k1", "k1", "k2", "k3", "k4", "k2", "k1", "k5", "k5"],
		    A: [5, 3, 12, 1, 50, 0, 1, 10, 600, -50].collect { it.doubleValue() },
			B: [5, 6, 2, 4,   5, 1, 8,  1,  30, 1].collect { it.doubleValue() },
		]
		Map outputValues = [
		    out: [1, 1.5, 7.5, 0.25, 10, 0, 0.375, 17.5, 20, -30].collect { it.doubleValue() },
			map: [
			    [k1: [out: 1d]],
				[k1: [out: 1.5d]],
				[k1: [out: 7.5d]],
				[k1: [out: 7.5d], k2: [out: 0.25d]],
				[k1: [out: 7.5d], k2: [out: 0.25d], k3: [out: 10d]],
				[k1: [out: 7.5d], k2: [out: 0.25d], k3: [out: 10d], k4: [out: 0d]],
				[k1: [out: 7.5d], k2: [out: 0.375d], k3: [out: 10d], k4: [out: 0d]],
				[k1: [out: 17.5d], k2: [out: 0.375d], k3: [out: 10d], k4: [out: 0d]],
				[k1: [out: 17.5d], k2: [out: 0.375d], k3: [out: 10d], k4: [out: 0d], k5: [out: 20d]],
				[k1: [out: 17.5d], k2: [out: 0.375d], k3: [out: 10d], k4: [out: 0d], k5: [out: -30d]],
			]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals { globals }
			.test()
	}

	def "forEach handles displayName correctly"() {
		modulesJson[1].outputs[0].displayName = "outout"
		def command = new SaveCanvasCommand(name: "sub-canvas", modules: modulesJson)
		def canvas = canvasService.createNew(command, user)

		module.getInput("canvas").receive(canvas.id)
		module.configure(module.getConfiguration())

		when:
		Map inputValues = [
			key: ["k1", "k1", "k1", "k2", "k3", "k4", "k2", "k1", "k5", "k5"],
			A: [5, 3, 12, 1, 50, 0, 1, 10, 600, -50].collect { it.doubleValue() },
			B: [5, 6, 2, 4,   5, 1, 8,  1,  30, 1].collect { it.doubleValue() },
		]
		Map outputValues = [
			outout: [1, 1.5, 7.5, 0.25, 10, 0, 0.375, 17.5, 20, -30].collect { it.doubleValue() },
			map: [
				[k1: [outout: 1d]],
				[k1: [outout: 1.5d]],
				[k1: [outout: 7.5d]],
				[k1: [outout: 7.5d], k2: [outout: 0.25d]],
				[k1: [outout: 7.5d], k2: [outout: 0.25d], k3: [outout: 10d]],
				[k1: [outout: 7.5d], k2: [outout: 0.25d], k3: [outout: 10d], k4: [outout: 0d]],
				[k1: [outout: 7.5d], k2: [outout: 0.375d], k3: [outout: 10d], k4: [outout: 0d]],
				[k1: [outout: 17.5d], k2: [outout: 0.375d], k3: [outout: 10d], k4: [outout: 0d]],
				[k1: [outout: 17.5d], k2: [outout: 0.375d], k3: [outout: 10d], k4: [outout: 0d], k5: [outout: 20d]],
				[k1: [outout: 17.5d], k2: [outout: 0.375d], k3: [outout: 10d], k4: [outout: 0d], k5: [outout: -30d]],
			]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals { globals }
			.test()
	}

	def "modules inside ForEach report correct runtime path"() {
		def command = new SaveCanvasCommand(name: "sub-canvas", modules: modulesJson)
		def canvas = canvasService.createNew(command, user)

		module.setHash(5)
		module.setParentSignalPath(Mock(SignalPath))
		module.getSignalPathByKey()

		module.getInput("canvas").receive(canvas.id)
		module.configure(module.getConfiguration())
		module.getInput("key").receive("k1")
		module.getInput("A").receive(5)
		module.getInput("B").receive(5)
		module.sendOutput()

		when:
		String path = module.getSignalPathByKey("k1").getModules()[0].getRuntimePath()
		then:
		1 * module.getParentSignalPath().getRuntimePath(_) >> { RuntimeRequest.PathWriter writer ->
			return writer.write("parent")
		}
		path == "parent/modules/5/keys/k1/modules/0"
	}
}
