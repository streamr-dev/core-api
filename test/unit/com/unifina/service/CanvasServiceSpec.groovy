package com.unifina.service

import com.unifina.api.InvalidStateException
import com.unifina.api.SaveCanvasCommand
import com.unifina.api.ValidationException
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.Module
import com.unifina.exceptions.CanvasUnreachableException
import com.unifina.signalpath.UiChannelIterator
import com.unifina.signalpath.charts.Heatmap
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonBuilder
import spock.lang.Specification

@TestFor(CanvasService)
@Mock([SecUser, Canvas, Module, ModuleService, SpringSecurityService, SignalPathService])
class CanvasServiceSpec extends Specification {

	SecUser me
	SecUser someoneElse
	Canvas myFirstCanvas

	Module moduleWithUi

	def setup() {
		moduleWithUi = new Module(implementingClass: Heatmap.name).save(validate: false)

		me = new SecUser(username: "me@me.com", apiKey: "myKey").save(validate: false)

		myFirstCanvas = new Canvas(
			name: "my_canvas_1",
			user: me,
			json: new JsonBuilder([
			    name: "my_canvas_1",
				hasExports: false,
				modules: [],
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

		new Canvas(
			name: "my_canvas_2",
			user: me,
			json: "{}",
		).save(failOnError: true)

		new Canvas(
			name: "my_canvas_3",
			user: me,
			json: "{}",
			adhoc: false,
			state: Canvas.State.RUNNING
		).save(failOnError: true)

		new Canvas(
			name: "my_canvas_4",
			user: me,
			json: "{}",
			adhoc: true,
			state: Canvas.State.RUNNING
		).save(failOnError: true)

		new Canvas(
			name: "my_canvas_5",
			user: me,
			json: "{}",
			adhoc: true,
			state: Canvas.State.STOPPED
		).save(failOnError: true)

		new Canvas(
			name: "my_canvas_6",
			user: me,
			json: "{}",
			adhoc: false,
			state: Canvas.State.STOPPED
		).save(failOnError: true)

		someoneElse = new SecUser(username: "someone@someone.com", apiKey: "otherKey").save(validate: false)

		new Canvas(
			name: "someoneElses_canvas_1",
			user: someoneElse,
			json: "{}",
			adhoc: false,
			state: Canvas.State.STOPPED
		).save(failOnError: true)

		new Canvas(
			name: "someoneElses_canvas_2",
			user: someoneElse,
			json: "{}",
		).save(failOnError: true)
	}

	def "#findAllBy, by default, lists all Canvases of current user"() {
		expect:
		service.findAllBy(me, null, null, null)*.name == (1..6).collect { "my_canvas_" + it }
	}

	def "#findAllBy can filter by name"() {
		expect:
		service.findAllBy(me, "my_canvas_4", null, null)*.name == ["my_canvas_4"]
	}

	def "#findAllBy can filter by adhoc"() {
		expect:
		service.findAllBy(me, null, false, null)*.name == [1,2,3,6].collect { "my_canvas_" + it }
		service.findAllBy(me, null, true, null)*.name == ["my_canvas_4", "my_canvas_5"]
	}

	def "#findAllBy can filter by state"() {
		expect:
		service.findAllBy(me, null, null, Canvas.State.RUNNING)*.name == ["my_canvas_3", "my_canvas_4"]
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

	def "createNew() creates a new Canvas"() {
		def command = new SaveCanvasCommand(name: "my_new_canvas", modules: [])

		when:
		def newId = service.createNew(command, me).id
		Canvas c = Canvas.findById(newId)

		then:
		c.id != null
		c.user == me
		c.name == "my_new_canvas"
		c.json != null // TODO: JSON Schema validation
		c.state == Canvas.State.STOPPED
		!c.hasExports
		!c.example

		c.runner == null
		c.server == null
		c.requestUrl == null
		!c.adhoc
		c.serialized == null
		c.serializationTime == null

		List uiChannelIds = uiChannelIdsFromMap(c.toMap())
		uiChannelIds.size() == 1
		uiChannelIds[0] != null
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
		service.updateExisting(myFirstCanvas, command)
		Canvas c = Canvas.findById(myFirstCanvas.id)

		then:
		c.user == me
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
		service.updateExisting(canvas, updateCommand)
		def updatedUiChannelIds = uiChannelIdsFromMap(canvas.toMap())

		then:
		updatedUiChannelIds.size() == 6
		updatedUiChannelIds.containsAll(existingUiChannelIds)
	}

	def "updateExisting clears serialization"() {
		setup:
		myFirstCanvas.serialized = "{}"
		myFirstCanvas.serializationTime = new Date()

		when:
		def command = new SaveCanvasCommand(
			name: "my_updated_canvas",
			modules: [],
			settings: ["a" : "b"]
		)
		service.updateExisting(myFirstCanvas, command)

		then:
		Canvas c = Canvas.findById(myFirstCanvas.id)
		c.serialized == null
		c.serializationTime == null

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
		service.updateExisting(myFirstCanvas, command)

		then:
		thrown(InvalidStateException)
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
		service.start(myFirstCanvas, false)

		then:
		1 * signalPathService.startLocal(myFirstCanvas, [speed: 0, beginDate: "2016-01-25", endDate: "2016-01-26"])
		0 * signalPathService._
	}

	def "start() invokes SignalPathService's startLocal() and clearState() given clear argument true"() {
		def signalPathService = Mock(SignalPathService)
		service.signalPathService = signalPathService

		when:
		service.start(myFirstCanvas, true)

		then:
		1 * signalPathService.clearState(myFirstCanvas)
		1 * signalPathService.startLocal(myFirstCanvas, [speed: 0, beginDate: "2016-01-25", endDate: "2016-01-26"])
		0 * signalPathService._
	}

	def "start() raises InvalidStateException if canvas running"() {
		def signalPathService = Mock(SignalPathService)
		service.signalPathService = signalPathService
		myFirstCanvas.state = Canvas.State.RUNNING
		myFirstCanvas.save(failOnError: true)

		when:
		service.start(myFirstCanvas, false)

		then:
		thrown(InvalidStateException)
	}

	def "start() raises ApiException about serialization if deserializing canvas fails"() {
		def signalPathService = Mock(SignalPathService)
		service.signalPathService = signalPathService
		myFirstCanvas.serialized = "serialized_content_be_here"
		myFirstCanvas.serializationTime = new Date()
		myFirstCanvas.save(failOnError: true)

		when:
		service.start(myFirstCanvas, false)

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

	def uiChannelIdsFromMap(Map signalPathMap) {
		UiChannelIterator.over(signalPathMap).collect { it.id }
	}
}
