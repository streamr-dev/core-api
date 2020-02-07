package com.unifina.controller.api

import com.unifina.ControllerSpecification
import com.unifina.api.*
import com.unifina.domain.security.*
import com.unifina.domain.signalpath.Canvas
import com.unifina.exceptions.CanvasUnreachableException
import com.unifina.security.AllowRole
import com.unifina.service.ApiService
import com.unifina.service.CanvasService
import com.unifina.service.SignalPathService
import com.unifina.exceptions.JavaCompilerErrorMessage
import com.unifina.signalpath.ModuleException
import com.unifina.exceptions.ModuleExceptionMessage

import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonBuilder

@TestFor(CanvasApiController)
@Mock([SecUser, SecUserSecRole, Permission, Canvas, Key])
class CanvasApiControllerSpec extends ControllerSpecification {

	ApiService apiService
	CanvasService canvasService
	SecUser me
	Canvas canvas1
	Canvas canvas2
	Canvas canvas3

	def setup() {
		controller.canvasService = canvasService = Mock(CanvasService)
		controller.signalPathService = Mock(SignalPathService)
		controller.apiService = apiService = Mock(ApiService)

		me = new SecUser(id: 1).save(validate: false)
		SecUser other = new SecUser(id: 2).save(validate: false)

		def k1 = new Key(name: "k1", user: me)
		k1.id = "myApiKey"
		k1.save(failOnError: true, validate: true)

		def k2 = new Key(name: "k2", user: other)
		k2.id = "otherApiKey"
		k2.save(failOnError: true, validate: true)

		canvas1 = new Canvas(
			name: "mine",
			json: new JsonBuilder([name: "mine", modules: [[hash: 1]], settings: [:]]).toString(),
			state: Canvas.State.STOPPED,
			hasExports: false
		)
		canvas1.save(validate: true, failOnError: true)

		canvas2 = new Canvas(
			name: "not mine",
			json: '{"name": "not mine", "modules": []}',
			state: Canvas.State.STOPPED,
			hasExports: false
		).save(validate: true, failOnError: true)

		canvas3 = new Canvas(
			name: "not mine but example",
			json: '{"name": "not mine but example", "modules": []}',
			state: Canvas.State.STOPPED,
			example: true,
			hasExports: false
		).save(validate: true, failOnError: true)

		assert SecUser.count() == 2
		assert Canvas.count() == 3
	}

	void "check annotations"() {
		when:
		Map<String, List<AllowRole>> actionRoles = [startAsAdmin: [AllowRole.ADMIN]]
		then:
		getInvalidAnnotations(CanvasApiController, actionRoles) == new HashSet()
	}

	void "index() renders authorized canvases as a list"() {
		when:
		authenticatedAs(me) { controller.index() }

		then:
		response.status == 200
		response.json.size() == 3
		1 * controller.apiService.list(Canvas, _, me) >> { clazz, ListParams listParams, user ->
			assert listParams.toMap() == new CanvasListParams().toMap()
			[canvas1, canvas2, canvas3]
		}
	}

	void "index() adds name param to filter criteria"() {
		when:
		params.name = "Foo"
		authenticatedAs(me) { controller.index() }

		then:
		response.status == 200
		response.json.size() == 0
		1 * controller.apiService.list(Canvas, _, me) >> { clazz, ListParams listParams, user ->
			assert listParams.toMap() == new CanvasListParams(name: "Foo").toMap()
			[]
		}
	}

	void "index() adds adhoc param to filter criteria"() {
		when:
		params.adhoc = "true"
		authenticatedAs(me) { controller.index() }

		then:
		response.status == 200
		response.json.size() == 0
		1 * controller.apiService.list(Canvas, _, me) >> { clazz, ListParams listParams, user ->
			assert listParams.toMap() == new CanvasListParams(adhoc: true).toMap()
			[]
		}
	}

	void "index() adds state param to filter criteria"() {
		when:
		params.state = "RUNNING"
		authenticatedAs(me) { controller.index() }

		then:
		response.status == 200
		response.json.size() == 0
		1 * controller.apiService.list(Canvas, _, me) >> { clazz, ListParams listParams, user ->
			assert listParams.toMap() == new CanvasListParams(state: Canvas.State.RUNNING).toMap()
			[]
		}
	}

	void "show() authorizes, reconstructs and renders the canvas as json"() {
		when:
		params.id = "1"
		authenticatedAs(me) { controller.show() }

		then:
		response.status == 200
		response.json?.size() > 0

		1 * canvasService.authorizedGetById("1", me, Permission.Operation.CANVAS_GET) >> canvas1
		1 * canvasService.reconstruct(canvas1, me) >> { Canvas c, SecUser user -> JSON.parse(c.json) }
	}

	void "show() supports runtime parameter"() {
		when:
		params.id = canvas1.id
		params.runtime = true
		authenticatedAs(me) { controller.show() }

		then:
		response.status == 200
		response.json?.size() > 0

		1 * canvasService.authorizedGetById("1", me, Permission.Operation.CANVAS_GET) >> canvas1
		1 * controller.signalPathService.runtimeRequest(_, false) >> [success:true, json:JSON.parse(canvas1.json)]
	}

	void "show() lets load a canvas in invalid state"() {
		setup:
		List<ModuleExceptionMessage> msgs = new ArrayList<>()
		msgs.add(new JavaCompilerErrorMessage(1, 11, "stupid programmer error"))
		msgs.add(new JavaCompilerErrorMessage(1, 10, "syntax error"))
		msgs.add(new JavaCompilerErrorMessage(2, 100, "syntax terror"))

		when:
		params.id = canvas1.id
		authenticatedAs(me) { controller.show() }

		then:
		response.status == 200
		response.json.size() > 0
		1 * canvasService.authorizedGetById("1", me, Permission.Operation.CANVAS_GET) >> canvas1
		1 * controller.canvasService.reconstruct(_, _) >> { throw new ModuleException("mocked", null, msgs) }
		response.json.moduleErrors[0].module == 1
		response.json.moduleErrors[0].line == 11
		response.json.moduleErrors[0].message == "stupid programmer error"
		response.json.moduleErrors[1].module == 1
		response.json.moduleErrors[1].line == 10
		response.json.moduleErrors[1].message == "syntax error"
		response.json.moduleErrors[2].module == 2
		response.json.moduleErrors[2].line == 100
		response.json.moduleErrors[2].message == "syntax terror"
	}

	void "save() creates a new canvas and renders it as json"() {
		def newCanvasId

		when:
		request.JSON = [
			name: "brand new Canvas",
			modules: [],
		]
		authenticatedAs(me) { controller.save() }

		then:
		response.status == 200
		response.json.id == newCanvasId
		1 * canvasService.createNew(_, me) >> { SaveCanvasCommand command, SecUser user ->
			assert command.name == "brand new Canvas"
			assert command.modules == []
			def c = new Canvas().save(validate: false)
			newCanvasId = c.id
			return c
		}
	}

	void "update() authorizes, updates and renders the canvas as json"() {
		when:
		params.id = "1"
		request.JSON = [
			name: "updated, new name",
			modules: [],
		]
		request.method = "PUT"
		authenticatedAs(me) { controller.update() }

		then:
		response.status == 200
		response.json?.size() > 0
		1 * canvasService.authorizedGetById("1", me, Permission.Operation.CANVAS_EDIT) >> canvas1
		1 * canvasService.updateExisting(canvas1, _, me) >> { Canvas canvas, SaveCanvasCommand command, SecUser user ->
			assert command.name == "updated, new name"
			assert command.modules == []
		}
	}

	void "update() must not update canvas if authorization fails"() {
		Map originalProperties = canvas2.properties

		when:
		params.id = canvas2.id
		request.JSON = [
			name: "me me me",
			modules: []
		]
		request.method = "PUT"
		authenticatedAs(me) { controller.update() }

		then: "canvas must be unchanged"
		originalProperties == canvas2.properties

		and: "exception must not be swallowed"
		thrown NotPermittedException
		1 * canvasService.authorizedGetById(canvas2.id, me, Permission.Operation.CANVAS_EDIT) >> { throw new NotPermittedException("mock") }
	}

	def "update() lets save canvas in invalid state"() {
		setup:
		List<ModuleExceptionMessage> msgs = new ArrayList<>()
		msgs.add(new JavaCompilerErrorMessage(1, 11, "stupid programmer error"))
		msgs.add(new JavaCompilerErrorMessage(1, 10, "syntax error"))
		msgs.add(new JavaCompilerErrorMessage(2, 100, "syntax terror"))

		params.id = "1"
		request.JSON = [
			name: "broken canvas",
			modules: [],
		]
		request.method = "PUT"

		when:
		authenticatedAs(me) { controller.update() }

		then:
		response.status == 200
		1 * canvasService.authorizedGetById("1", me, Permission.Operation.CANVAS_EDIT) >> canvas1
		1 * canvasService.updateExisting(canvas1, _, me) >> { throw new ModuleException("mocked", null, msgs) }
		response.json.moduleErrors[0].module == 1
		response.json.moduleErrors[0].line == 11
		response.json.moduleErrors[0].message == "stupid programmer error"
		response.json.moduleErrors[1].module == 1
		response.json.moduleErrors[1].line == 10
		response.json.moduleErrors[1].message == "syntax error"
		response.json.moduleErrors[2].module == 2
		response.json.moduleErrors[2].line == 100
		response.json.moduleErrors[2].message == "syntax terror"
	}


	void "delete() must authorize and delete the canvas"() {
		when:
		params.id = "1"
		request.method = "DELETE"
		authenticatedAs(me) { controller.delete() }

		then:
		response.status == 204
		1 * canvasService.authorizedGetById("1", me, Permission.Operation.CANVAS_DELETE) >> canvas1
		1 * canvasService.deleteCanvas(canvas1, me)
	}

	void "delete() must not delete the canvas if authorization fails"() {
		when:
		params.id = "2"
		request.method = "DELETE"
		authenticatedAs(me) { controller.delete() }

		then:
		thrown NotPermittedException
		1 * canvasService.authorizedGetById("2", me, Permission.Operation.CANVAS_DELETE) >> { throw new NotPermittedException("mock") }
		Canvas.get("2") != null
	}

	void "start() must authorize and start a canvas"() {
		when:
		params.id = "1"
		request.method = "POST"
		authenticatedAs(me) { controller.start() }

		then:
		response.status == 200
		response.json?.size() > 0
		1 * canvasService.authorizedGetById("1", me, Permission.Operation.CANVAS_STARTSTOP) >> canvas1
		1 * canvasService.start(canvas1, false, me)
	}

	void "start() must authorize and be able to start a Canvas with clearing enabled"() {
		when:
		params.id = "1"
		request.JSON = [clearState: true]
		request.method = "POST"
		authenticatedAs(me) { controller.start() }

		then:
		response.status == 200
		response.json?.size() > 0
		1 * canvasService.authorizedGetById("1", me, Permission.Operation.CANVAS_STARTSTOP) >> canvas1
		1 * canvasService.start(canvas1, true, me)
	}

	void "start() must not start a canvas if authorization fails"() {
		Map originalProperties = canvas2.properties

		when:
		params.id = canvas2.id
		request.method = "POST"
		authenticatedAs(me) { controller.start() }

		then: "canvas must be unchanged"
		originalProperties == canvas2.properties

		and: "exception must not be swallowed"
		thrown NotPermittedException
		1 * canvasService.authorizedGetById(canvas2.id, me, Permission.Operation.CANVAS_STARTSTOP) >> {throw new NotPermittedException("mock")}
	}

	void "startAsAdmin() requires parameter startedBy to be given"() {
		setup:
		def adminRole = new SecRole(authority: "ROLE_ADMIN").save(failOnError: true, validate: false)
		new SecUserSecRole(secUser: me, secRole: adminRole).save(failOnError: true, validate: false)

		when:
		params.id = "1"
		request.method = "POST"
		authenticatedAs(me) { controller.startAsAdmin() }

		then:
		def e = thrown(ValidationException)
		e.message.contains("nullable")
	}

	void "startAsAdmin() requires parameter startedBy to be an existing user id"() {
		setup:
		def adminRole = new SecRole(authority: "ROLE_ADMIN").save(failOnError: true, validate: false)
		new SecUserSecRole(secUser: me, secRole: adminRole).save(failOnError: true, validate: false)

		when:
		params.id = "1"
		params.startedBy = 6
		request.method = "POST"
		authenticatedAs(me) { controller.startAsAdmin() }

		then:
		def e = thrown(ValidationException)
		e.message.contains("typeMismatch") // typeMismatch means Grails didn't find SecUser for id
	}

	void "startAsAdmin() starts a Canvas if user is admin"() {
		setup:
		def adminRole = new SecRole(authority: "ROLE_ADMIN").save(failOnError: true, validate: false)
		new SecUserSecRole(secUser: me, secRole: adminRole).save(failOnError: true, validate: false)

		def someoneElse = new SecUser(username: "someoneElse@streamr.com").save(failOnError: true, validate: false)

		when:
		params.id = "1"
		params.startedBy = someoneElse.id
		request.method = "POST"
		authenticatedAs(me) { controller.startAsAdmin() }

		then:
		1 * apiService.getByIdAndThrowIfNotFound(Canvas, "1") >> canvas1
		1 * canvasService.start(canvas1, false, someoneElse)

		and:
		response.status == 200
		response.json?.size() > 0
	}

	void "stop() must authorize and stop a canvas"() {
		when:
		params.id = "1"
		request.method = "POST"
		authenticatedAs(me) { controller.stop() }

		then:
		response.status == 200
		response.json?.size() > 0
		1 * canvasService.authorizedGetById("1", me, Permission.Operation.CANVAS_STARTSTOP) >> canvas1
		1 * canvasService.stop(canvas1, me)
	}

	void "stop() must return 204 for adhoc canvases"() {
		canvas1.adhoc = true

		when:
		params.id = "1"
		request.method = "POST"
		authenticatedAs(me) { controller.stop() }

		then:
		response.status == 204
		1 * canvasService.authorizedGetById("1", me, Permission.Operation.CANVAS_STARTSTOP) >> canvas1
		1 * canvasService.stop(canvas1, me)
	}

	void "stop() must not stop the canvas if authorization fails"() {
		Map originalProperties = canvas2.properties

		when:
		params.id = canvas2.id
		request.method = "POST"
		authenticatedAs(me) { controller.stop() }

		then: "canvas must be unchanged"
		originalProperties == canvas2.properties

		and: "exception must not be swallowed"
		thrown NotPermittedException
		1 * canvasService.authorizedGetById(canvas2.id, me, Permission.Operation.CANVAS_STARTSTOP) >> {throw new NotPermittedException("mock")}
	}

	void "stop() must throw an exception if the canvas can't be reached"() {
		when:
		params.id = "1"
		request.method = "POST"
		authenticatedAs(me) { controller.stop() }

		then:
		thrown(CanvasUnreachableException)
		1 * canvasService.authorizedGetById("1", me, Permission.Operation.CANVAS_STARTSTOP) >> canvas1
		1 * canvasService.stop(canvas1, me) >> { throw new CanvasUnreachableException("Test message") }

	}

	void "module() must authorize and render the result as json"() {
		def result = JSON.parse(canvas1.json).modules.find {it.hash == 1}

		when:
		params.canvasId = "1"
		params.moduleId = 1
		params.dashboardId = "2"
		request.method = "GET"
		authenticatedAs(me) { controller.module() }

		then:
		response.status == 200
		response.json == result
		1 * canvasService.authorizedGetModuleOnCanvas("1", 1, "2", me, Permission.Operation.CANVAS_GET) >> result
	}

	void "module() supports runtime parameter"() {
		def result = JSON.parse(canvas1.json).modules.find {it.hash == 1}

		when:
		params.canvasId = "1"
		params.moduleId = 1
		params.dashboardId = "2"
		params.runtime = true
		request.method = "GET"
		authenticatedAs(me) { controller.module() }

		then:
		response.status == 200
		response.json == result
		1 * controller.signalPathService.runtimeRequest(_, false) >> [success:true, json:result]
	}

	void "runtimeRequest() must build and send a runtime request to the canvas"() {
		def runtimeResponse = [foo: 'bar']

		when:
		params.id = canvas1.id
		params.path = canvas1.id
		request.JSON = [bar: 'foo']
		request.method = "POST"
		authenticatedAs(me) { controller.runtimeRequest() }

		then:
		response.status == 200
		response.json == runtimeResponse
		1 * controller.signalPathService.buildRuntimeRequest([bar: 'foo'], "canvases/$canvas1.id", me)
		1 * controller.signalPathService.runtimeRequest(_, false) >> runtimeResponse
	}

	void "runtimeRequest() must force a local request if params.local is true"() {
		def runtimeResponse = [foo: 'bar']

		when:
		params.id = canvas1.id
		params.path = canvas1.id
		params.local = "true"
		request.JSON = [bar: 'foo']
		request.method = "POST"
		authenticatedAs(me) { controller.runtimeRequest() }

		then:
		response.status == 200
		response.json == runtimeResponse
		1 * controller.signalPathService.runtimeRequest(_, true) >> runtimeResponse
	}

	void "validateFilename() doesnt accept dot dot paths or slashs"(String filename, Boolean expected) {
		expect:
		controller.validateFilename(filename) == expected
		where:
		filename | expected
		null | false
		"" | false
		" " | false
		"\t" | false
		"\n" | false
		"../filename.txt" | false
		"/file.csv" | false
		"file/name.csv" | false
		"..data.txt" | false
		"../../etc/passwd" | false
		"data.txt" | false // wrong extension
		"streamr_csv_0.csv" | true
		"streamr_csv_111112222233333.csv" | true
		"streamr_csv_6043894978795937897.csv" | true
		"streamr_csv_1111122222333334444.csv" | true // 19 digits
		"streamr_csv_11111222223333344444.csv" | false // 20 digits
	}

	void "downloadCsv() doesnt let users define dot dot slash paths"() {
		when:
		params.filename = "../filename.txt"
		request.method = "GET"
		authenticatedAs(me) { controller.downloadCsv() }
		then:
		def e = thrown(BadRequestException)
		e.getStatusCode() == 400
		e.getCode() == "INVALID_PARAMETER"
		e.getMessage() == "filename contains illegal characters"
	}

	void "downloadCsv() downloads csv file"() {
		setup:
		String data = "file,csv,data\n1,2,3\n"
		File file = File.createTempFile("streamr_csv_", ".csv")
		file.write(data)
		String fileLen = file.length() + ""
		when:
		params.filename = file.getName()
		request.method = "GET"
		authenticatedAs(me) { controller.downloadCsv() }
		then:
		response.getText() == data
		response.status == 200
		response.contentType == "text/csv"
		response.getHeader("Content-Disposition") == "attachment; filename=" + file.getName()
		response.getHeader("Content-Length") == fileLen
	}
}
