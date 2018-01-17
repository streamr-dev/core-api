package com.unifina.signalpath.list

import com.unifina.api.SaveCanvasCommand
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.Module
import com.unifina.service.CanvasService
import com.unifina.service.ModuleService
import com.unifina.service.PermissionService
import com.unifina.service.SignalPathService
import com.unifina.signalpath.simplemath.Divide
import com.unifina.signalpath.simplemath.Sum
import com.unifina.utils.Globals
import com.unifina.utils.GlobalsFactory
import com.unifina.utils.testutils.ModuleTestHelper
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
@Mock([Canvas, Module, SecUser, ModuleService, SpringSecurityService, SignalPathService, CanvasService, PermissionService])
class ForEachItemSpec extends Specification {

	CanvasService canvasService

	Globals globals
	ForEachItem module
	SecUser user

	def modulesJson

	def setup() {
		canvasService = mainContext.getBean(CanvasService)
		canvasService.signalPathService = mainContext.getBean(SignalPathService)
		module = new ForEachItem()
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

	def "forEachItem works correctly with state clearing"() {
		def command = new SaveCanvasCommand(name: "sub-canvas", modules: modulesJson)
		def canvas = canvasService.createNew(command, user)

		module.getInput("canvas").receive(canvas.id)
		module.configure(module.getConfiguration())

		when:
		Map inputValues = [
			A: [[1d], [2d, 16d, 18d], [0d, 3d, 1d]],
			B: [[1d], [4d, 8d,  3d], [1d, 1d, 2d]],
		]
		Map outputValues = [
			out: [
				[1d],
				[(2d/4d), (2d/4d) + (16d / 8d), (2d/4d) + (16d / 8d) + (18 / 3d)],
				[0d, 3d, 3d + 0.5d]
			],
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals { globals }
			.test()
	}

	def "forEachItem works correctly w/o state clearing"() {
		def command = new SaveCanvasCommand(name: "sub-canvas", modules: modulesJson)
		def canvas = canvasService.createNew(command, user)

		module.getInput("keepState").receive(true)
		module.getInput("canvas").receive(canvas.id)
		module.configure(module.getConfiguration())

		when:
		Map inputValues = [
			A: [[1d], [2d, 16d, 18d], [0d, 3d, 1d]],
			B: [[1d], [4d, 8d,  3d], [0d, 1d, 2d]],
		]
		Map outputValues = [
			out: [
				[1d],
				[1d + (2d/4d), 1d + (2d/4d) + (16d / 8d), 1d + (2d/4d) + (16d / 8d) + (18 / 3d)],
				[9.5d, 9.5d + 3d, 9.5d + 3d + 0.5d]
			],
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
			.overrideGlobals { globals }
			.test()
	}
}
