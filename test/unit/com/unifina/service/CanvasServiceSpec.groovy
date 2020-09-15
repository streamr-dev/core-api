package com.unifina.service

import com.unifina.BeanMockingSpecification
import com.unifina.api.*
import com.unifina.domain.*
import com.unifina.signalpath.ModuleException
import com.unifina.signalpath.UiChannelIterator
import com.unifina.signalpath.charts.Heatmap
import com.unifina.task.CanvasDeleteTask
import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.codehaus.groovy.grails.web.mapping.LinkGenerator

@TestMixin(ControllerUnitTestMixin) // "as JSON" converter
@TestFor(CanvasService)
@Mock([User, Canvas, Module, Permission, Serialization, Dashboard, DashboardItem])
class CanvasServiceSpec extends BeanMockingSpecification {

	User me
    User someoneElse
	Canvas myFirstCanvas
	List<Canvas> canvases = []
	Module moduleWithUi

	String json = new JsonBuilder([
		modules: [],
		settings: [:],
	]).toPrettyString()

	PermissionService permissionService = Mock(PermissionService)

	def setup() {
		service.permissionService = mockBean(PermissionService, permissionService)
		service.permissionService.check(_, _, _) >> true
		service.dashboardService = mockBean(DashboardService, Mock(DashboardService))
		service.streamService = mockBean(StreamService, Mock(StreamService))
		service.streamService.createStream(_,_,_) >> new Stream()
		mockBean(ModuleService, new ModuleService())
		service.signalPathService = mockBean(SignalPathService, new SignalPathService())

		moduleWithUi = new Module(implementingClass: Heatmap.name).save(validate: false)

		me = new User(username: "me@me.com").save(validate: false)

		myFirstCanvas = new Canvas(
			name: "my_canvas_1",
			json: new JsonBuilder([
			    name: "my_canvas_1",
				hasExports: false,
				modules: [
						[hash: 1]
				],
				settings: [
					speed: 0,
					beginDate: "2016-01-25",
					endDate: "2016-01-26",
				],
				uiChannel: [
				    id: "666",
					name: "Notifications"
				]
			]).toString(),
		)

		myFirstCanvas.save(failOnError: true)
		permissionService.systemGrantAll(me, myFirstCanvas)
		canvases << myFirstCanvas

		canvases << new Canvas(
			name: "my_canvas_2",
			json: json,
		).save(failOnError: true)

		canvases << new Canvas(
			name: "my_canvas_3",
			adhoc: false,
			state: Canvas.State.RUNNING,
			json: json,
		).save(failOnError: true)

		canvases << new Canvas(
			name: "my_canvas_4",
			adhoc: true,
			state: Canvas.State.RUNNING,
			json: json,
		).save(failOnError: true)

		canvases << new Canvas(
			name: "my_canvas_5",
			adhoc: true,
			state: Canvas.State.STOPPED,
			json: json,
		).save(failOnError: true)

		canvases << new Canvas(
			name: "my_canvas_6",
			adhoc: false,
			state: Canvas.State.STOPPED,
			json: json,
		).save(failOnError: true)

		someoneElse = new User(username: "someone@someone.com").save(validate: false)

		canvases << new Canvas(
			name: "someoneElses_canvas_1",
			adhoc: false,
			state: Canvas.State.STOPPED,
			json: json,
		).save(failOnError: true)

		canvases << new Canvas(
			name: "someoneElses_canvas_2",
			json: json,
		).save(failOnError: true)
	}

	def "add example shared canvases"() {
		setup:
		Canvas c0 = new Canvas(
			name: "example canvas",
			exampleType: ExampleType.SHARE
		).save(failOnError: true)
		canvases << c0
		Canvas c1 = new Canvas(
			name: "example 2 canvas",
			exampleType: ExampleType.SHARE
		).save(failOnError: true)
		canvases << c1

		when:
		service.addExampleCanvases(me, canvases)
		then:
		notThrown(RuntimeException)
	}

	def "add example copy canvases"() {
		setup:
		service.streamService = Mock(StreamService)
		Canvas c0 = new Canvas(
			name: "example canvas",
			exampleType: ExampleType.COPY,
			json: json,
		)
		c0.id = "c0"
		c0.save(failOnError: true)
		canvases << c0
		Canvas c1 = new Canvas(
			name: "example 2 canvas",
			exampleType: ExampleType.COPY,
			json: json,
		)
		c1.id = "c1"
		c1.save(failOnError: true)
		canvases << c1

		when:
		service.addExampleCanvases(me, canvases)
		then:
		notThrown(RuntimeException)
	}

	def "add example copy and share canvases"() {
		setup:
		service.streamService = Mock(StreamService)
		Canvas c0 = new Canvas(
			name: "example canvas",
			exampleType: ExampleType.COPY,
			json: json,
		)
		c0.id = "c0"
		c0.save(failOnError: true)
		canvases << c0
		Canvas c1 = new Canvas(
			name: "example 2 canvas",
			exampleType: ExampleType.SHARE,
			json: json,
		)
		c1.id = "c1"
		c1.save(failOnError: true)
		canvases << c1

		when:
		service.addExampleCanvases(me, canvases)
		then:
		notThrown(RuntimeException)
	}

	def "createNew() throws error when given null command object"() {
		when:
		service.createNew(null, me)

		then:
		thrown(NullPointerException)
	}

	def "createNew() throws error when given incomplete command object"() {
		when:
		service.createNew(new SaveCanvasCommand(), me)

		then:
		thrown(ValidationException)
	}

	def "createNew() replaces empty name with default value"() {
		when:
		Canvas c = service.createNew(new SaveCanvasCommand(name: "", modules: []), me)

		then:
		c.name == "Untitled Canvas"
	}

	def "createNew() creates a new Canvas"() {
		def command = new SaveCanvasCommand(name: "my_new_canvas", modules: [])

		when:
		def newId = service.createNew(command, me).id
		Canvas c = Canvas.findById(newId)

		then:
		c.id != null
		c.name == "my_new_canvas"
		c.json != null // TODO: JSON Schema validation
		c.state == Canvas.State.STOPPED
		!c.hasExports

		c.runner == null
		c.server == null
		c.requestUrl == null
		!c.adhoc
		c.serialization == null

		List uiChannelIds = uiChannelIdsFromMap(c.toMap())
		uiChannelIds.size() == 1
		uiChannelIds[0] != null
	}

	def "createNew() grants all permissions on Canvas for user via PermissionService"() {
		def command = new SaveCanvasCommand(name: "my_new_canvas", modules: [])

		when:
		Canvas canvas = service.createNew(command, me)

		then:
		notThrown(RuntimeException)
	}

	def "createNew() creates a new adhoc Canvas when given adhoc setting"() {
		def command = new SaveCanvasCommand(name: "my_new_canvas", modules: [], adhoc: true)

		when:
		def newId = service.createNew(command, me).id
		Canvas c = Canvas.findById(newId)

		then:
		c.adhoc
	}

	def "updateExisting updates existing Canvas keeping existing uiChannelIds intact"() {
		def command = new SaveCanvasCommand(
			name: "my_updated_canvas",
			modules: [],
			settings: ["a" : "b"]
		)

		when:
		service.updateExisting(myFirstCanvas, command, me)
		Canvas c = Canvas.findById(myFirstCanvas.id)

		then:
		c.name == "my_updated_canvas"
		c.json != null // TODO: JSON Schema validation

		List uiChannelIds = uiChannelIdsFromMap(myFirstCanvas.toMap())
		uiChannelIds.size() == 1
		uiChannelIds[0] == "666"
	}

	def "updateExisting creates uiChannels for new modules and keeps old ones intact"() {
		def createCommand = new SaveCanvasCommand(
			name: "my_canvas_with_modules",
			modules: [
			    [id: moduleWithUi.id, params: [], inputs: [], outputs: []],
				[id: moduleWithUi.id, params: [], inputs: [], outputs: []],
				[id: moduleWithUi.id, params: [], inputs: [], outputs: []]
			]
		)
		def canvas = service.createNew(createCommand, me)
		def modules = canvas.toMap().modules

		def existingUiChannelIds = uiChannelIdsFromMap(canvas.toMap())
		assert existingUiChannelIds.size() == 4

		when:
		def newModules = [
		    [id: moduleWithUi.id, params: [], inputs: [], outputs: []],
			[id: moduleWithUi.id, params: [], inputs: [], outputs: []]
		]
		def updateCommand = new SaveCanvasCommand(
			name: "my_canvas_with_modules",
			modules: newModules + modules
		)
		service.updateExisting(canvas, updateCommand, me)
		def updatedUiChannelIds = uiChannelIdsFromMap(canvas.toMap())

		then:
		updatedUiChannelIds.size() == 6
		updatedUiChannelIds.containsAll(existingUiChannelIds)
	}

	def "updateExisting clears serialization"() {
		setup:
		myFirstCanvas.serialization = new Serialization(date: new Date(), bytes: new byte[12])
		myFirstCanvas.save(failOnError: true)
		def serializationId = myFirstCanvas.serialization.id
		assert serializationId != null

		when:
		def command = new SaveCanvasCommand(
			name: "my_updated_canvas",
			modules: [],
			settings: ["a" : "b"]
		)
		service.updateExisting(myFirstCanvas, command, me)

		then:
		Canvas c = Canvas.findById(myFirstCanvas.id)
		c.serialization == null

		and:
		Serialization s = Serialization.findById(serializationId)
		s == null

	}

	def "updateExisting throws error if trying to update running canvas"() {
		setup:
		myFirstCanvas.state = Canvas.State.RUNNING

		when:
		def command = new SaveCanvasCommand(
			name: "my_updated_canvas",
			modules: [],
			settings: ["a" : "b"]
		)
		service.updateExisting(myFirstCanvas, command, me)

		then:
		thrown(InvalidStateException)
	}

	def "updateExisting lets save canvas in invalid state"() {
		setup:
		service.signalPathService = Mock(SignalPathService)
		def cmd = new SaveCanvasCommand(
			name: "canvas",
			modules: [
				[id: 22, params: [], inputs: [], outputs: []],
			],
			settings: [
			    foo: "bar",
			],
		)

		when:
		service.updateExisting(myFirstCanvas, cmd, me)

		then:
		Canvas result = Canvas.findById(myFirstCanvas.id)
		result.name == "canvas"
		Map json = new JsonSlurper().parseText(result.json)
		json.settings.foo == "bar"
		json.name == "canvas"
		json.hasExports == false
		json.uiChannel.name == "Notifications"
		json.uiChannel.id == "666"
		json.modules[0].id == 22
		1 * service.signalPathService.reconstruct(_, _) >> { throw new ModuleException("mocked", null, null) }
		thrown ModuleException
	}

	def "createNew always generates new uiChannels"() {
		def createCommand = new SaveCanvasCommand(
			name: "my_canvas_with_modules",
			modules: [
				[id: moduleWithUi.id, params: [], inputs: [], outputs: []],
				[id: moduleWithUi.id, params: [], inputs: [], outputs: []],
				[id: moduleWithUi.id, params: [], inputs: [], outputs: []]
			]
		)
		def canvas = service.createNew(createCommand, me)
		def modules = canvas.toMap().modules

		def existingUiChannelIds = uiChannelIdsFromMap(canvas.toMap()) as Set
		assert existingUiChannelIds.size() == 4

		when:
		def create2ndCommand = new SaveCanvasCommand(name: "my_canvas_with_modules", modules: modules)
		def newCanvas = service.createNew(create2ndCommand, me)
		def newUiChannelIds = uiChannelIdsFromMap(newCanvas.toMap())

		then:

		newUiChannelIds.size() == 4
		existingUiChannelIds.size() == 4
		existingUiChannelIds.plus(newUiChannelIds).size() == 8
		existingUiChannelIds.intersect(newUiChannelIds).empty
	}

	def "start() invokes SignalPathService.startLocal()"() {
		def signalPathService = Mock(SignalPathService)
		service.signalPathService = signalPathService

		when:
		service.start(myFirstCanvas, false, me)

		then:
		1 * signalPathService.startLocal(myFirstCanvas, [speed: 0, beginDate: "2016-01-25", endDate: "2016-01-26"], me)
		0 * signalPathService._
	}

	def "start() invokes SignalPathService's startLocal() and clearState() given clear argument true"() {
		def signalPathService = Mock(SignalPathService)
		service.signalPathService = signalPathService

		when:
		service.start(myFirstCanvas, true, me)

		then:
		1 * signalPathService.clearState(myFirstCanvas)
		1 * signalPathService.startLocal(myFirstCanvas, [speed: 0, beginDate: "2016-01-25", endDate: "2016-01-26"], me)
		0 * signalPathService._
	}

	def "start() raises InvalidStateException if canvas running"() {
		def signalPathService = Mock(SignalPathService)
		service.signalPathService = signalPathService
		myFirstCanvas.state = Canvas.State.RUNNING
		myFirstCanvas.save(failOnError: true)

		when:
		service.start(myFirstCanvas, false, me)

		then:
		thrown(InvalidStateException)
	}

	def "start() raises ApiException about serialization if deserializing canvas fails"() {
		def signalPathService = Mock(SignalPathService)
		service.signalPathService = signalPathService
		myFirstCanvas.serialization = new Serialization(date: new Date(), bytes: "invalid_content_be_here".bytes)
		myFirstCanvas.save(failOnError: true)

		when:
		service.start(myFirstCanvas, false, me)

		then:
		true
	}

	def "stop() invokes SignalPathService.stopRemote()"() {
		def signalPathService = Mock(SignalPathService)

		service.signalPathService = signalPathService
		myFirstCanvas.state = Canvas.State.RUNNING
		myFirstCanvas.save(failOnError: true)

		when:
		service.stop(myFirstCanvas, me)

		then:
		1 * signalPathService.stopRemote(myFirstCanvas, me) >> [:]
		0 * signalPathService._

	}

	def "stop() throws CanvasUnreachableException is canvas cannot be reached"() {
		def signalPathService = Stub(SignalPathService)
		service.signalPathService = signalPathService
		myFirstCanvas.state = Canvas.State.RUNNING

		signalPathService.stopRemote(myFirstCanvas, me) >> { throw new CanvasUnreachableException("") }

		when:
		service.stop(myFirstCanvas, me)

		then:
		thrown(CanvasUnreachableException)
	}

	def "stop() throws InvalidStateException if canvas is not running"() {
		def signalPathService = Stub(SignalPathService)
		service.signalPathService = signalPathService
		myFirstCanvas.state = Canvas.State.STOPPED

		signalPathService.stopRemote(myFirstCanvas, me) >> { throw new CanvasUnreachableException("") }

		when:
		service.stop(myFirstCanvas, me)

		then:
		thrown(InvalidStateException)
	}

	def "deleteCanvas(,,false) deletes canvas"() {
		setup:
		assert Canvas.findById(myFirstCanvas.id) != null
		when:
		service.deleteCanvas(myFirstCanvas, me, false)
		then:
		Canvas.findById(myFirstCanvas.id) == null
	}

	def "deleteCanvas(,,false) deletes uiChannels of canvas"() {
		setup:
		Stream s = new Stream(
			id: "666",
			name: "Notifications",
			uiChannel: true,
			uiChannelCanvas: myFirstCanvas,
			uiChannelPath: "/canvas/1",
		)
		s.id = "666"
		s.save(failOnError: true, validate: false, flush: true)

		def streamService = service.streamService = Mock(StreamService)

		when:
		service.deleteCanvas(myFirstCanvas, me, false)
		then:
		1 * streamService.deleteStream(s)
	}

	def "deleteCanvas(,,true) creates a delete task"() {
		def taskService = service.taskService = Mock(TaskService)
		when:
		service.deleteCanvas(myFirstCanvas, me, true)
		then:
		1 * taskService.createTask(CanvasDeleteTask, [canvasId: '1'], "delete-canvas", me, _)
	}

	def "deleteCanvas(,,true) creates a delete task even if canvas running"() {
		def taskService = service.taskService = Mock(TaskService)
		myFirstCanvas.state = Canvas.State.RUNNING
		myFirstCanvas.save(failOnError: true, validate: true)

		when:
		service.deleteCanvas(myFirstCanvas, me, true)
		then:
		1 * taskService.createTask(CanvasDeleteTask, [canvasId: '1'], "delete-canvas", me, _)
	}

	def "deleteCanvas() throws ApiException if trying to (immediately) delete running canvas"() {
		when:
		myFirstCanvas.state = Canvas.State.RUNNING
		myFirstCanvas.save(failOnError: true, validate: true)

		service.deleteCanvas(myFirstCanvas, me, false)
		then:
		def e = thrown(ApiException)
		e.asApiError().statusCode == 409
	}

	def "authorizedGetById() checks access to canvases from PermissionService and returns the canvas if allowed"() {
		when:
		def canvas = service.authorizedGetById(myFirstCanvas.id, me, Permission.Operation.CANVAS_GET)

		then:
		canvas == myFirstCanvas
	}

	def "authorizedGetById() checks access to canvases from PermissionService and throws exception if not allowed"() {
		when:
		service.authorizedGetById(myFirstCanvas.id, me, Permission.Operation.CANVAS_GET)

		then:
		thrown NotPermittedException
		1 * service.permissionService.check(me, myFirstCanvas, Permission.Operation.CANVAS_GET) >> false
	}

	def "authorizedGetById() throws NotFoundException if no canvas exists"() {
		when:
		service.authorizedGetById("foo", me, Permission.Operation.CANVAS_GET)

		then:
		thrown(NotFoundException)
	}

	def "authorizedGetModuleOnCanvas() checks access to canvas from PermissionService and returns the module if allowed"() {
		when:
		def module = service.authorizedGetModuleOnCanvas(myFirstCanvas.id, 1, null, me, Permission.Operation.CANVAS_GET)

		then:
		module == JSON.parse(myFirstCanvas.json).modules.find {it.hash == 1}
	}

	def "authorizedGetModuleOnCanvas() checks access to dashboard from PermissionService and returns the module if allowed"() {
		setup:
		permissionService.systemRevoke(me, myFirstCanvas, Permission.Operation.CANVAS_GET)

		Dashboard db = new Dashboard().save(validate: false, failOnError: true)
		db.addToItems(new DashboardItem(canvas: myFirstCanvas, module: 1, title: "foo"))
		db.save(validate: false)

		when:
		def module = service.authorizedGetModuleOnCanvas(myFirstCanvas.id, 1, db.id, me, Permission.Operation.CANVAS_GET)

		then:
		module == JSON.parse(myFirstCanvas.json).modules.find {it.hash == 1}
		1 * service.permissionService.check(me, myFirstCanvas, Permission.Operation.CANVAS_GET) >> false
		1 * service.dashboardService.authorizedGetById(db.id, me, Permission.Operation.CANVAS_GET) >> db
	}

	def "authorizedGetModuleOnCanvas() checks access to dashboard from PermissionService and throws exception if the canvas doesn't match the dashboard item"() {
		setup:
		Dashboard db = new Dashboard().save(validate: false, failOnError: true)
		db.addToItems(new DashboardItem(canvas: canvases.find {it != myFirstCanvas}, module: 1, title: "foo"))
		db.save(validate: false)

		when:
		service.authorizedGetModuleOnCanvas(myFirstCanvas.id, 1, db.id, me, Permission.Operation.CANVAS_GET)

		then:
		1 * service.permissionService.check(me, myFirstCanvas, Permission.Operation.CANVAS_GET) >> false
		1 * service.dashboardService.authorizedGetById(db.id, me, Permission.Operation.CANVAS_GET) >> db
		thrown(NotPermittedException)
	}

	def "authorizedGetModuleOnCanvas() checks access to dashboard from PermissionService and throws exception if the module doesn't match the dashboard item"() {
		Dashboard db = new Dashboard()
		db.addToItems(new DashboardItem(canvas: myFirstCanvas, module: 999, ord: 0, title: "foo"))
		db.save(validate: false)
		service.permissionService = Mock(PermissionService)
		service.dashboardService = Mock(DashboardService)

		when:
		service.authorizedGetModuleOnCanvas(myFirstCanvas.id, 1, db.id, me, Permission.Operation.CANVAS_GET)

		then:
		1 * service.dashboardService.authorizedGetById(db.id, me, Permission.Operation.CANVAS_GET) >> db
		thrown(NotPermittedException)
	}

	def "authorizedGetModuleOnCanvas() checks access to canvases from PermissionService and throws exception if not allowed and no dashboard given"() {
		when:
		service.authorizedGetModuleOnCanvas(myFirstCanvas.id, 1, null, me, Permission.Operation.CANVAS_GET)

		then:
		1 * service.permissionService.check(me, myFirstCanvas, Permission.Operation.CANVAS_GET) >> false
		thrown(NotPermittedException)
	}

	def "authorizedGetModuleOnCanvas() throws NotFoundException if no canvas exists"() {
		when:
		service.authorizedGetModuleOnCanvas("foo", 1, null, me, Permission.Operation.CANVAS_GET)
		then:
		thrown(NotFoundException)
	}

	def "authorizedGetModuleOnCanvas() throws NotFoundException if no module exists"() {
		when:
		service.authorizedGetModuleOnCanvas(myFirstCanvas.id, 999, null, me, Permission.Operation.CANVAS_GET)

		then:
		thrown(NotFoundException)
	}

	def "authorizedGetModuleOnCanvas() throws NotFoundException if no dashboard exists"() {
		when:
		service.authorizedGetModuleOnCanvas(myFirstCanvas.id, 1, "1", me, Permission.Operation.CANVAS_GET)

		then:
		thrown(NotFoundException)
		1 * service.permissionService.check(me, myFirstCanvas, Permission.Operation.CANVAS_GET) >> false
		1 * service.dashboardService.authorizedGetById("1", me, Permission.Operation.CANVAS_GET) >> { throw new NotFoundException("thrown by mock") }
	}

	def "getCanvasURL() returns a link for the canvas"() {
		service.grailsLinkGenerator = Mock(LinkGenerator)

		when:
		def link = service.getCanvasURL(myFirstCanvas)

		then:
		1 * service.grailsLinkGenerator.link([controller: 'canvas', action: 'editor', id: myFirstCanvas.id, absolute: true]) >> "https://www.streamr.com/canvas/editor/1"
		link == "https://www.streamr.com/canvas/editor/1"
	}

	private uiChannelIdsFromMap(Map signalPathMap) {
		UiChannelIterator.over(signalPathMap).collect { it.id }
	}
}
