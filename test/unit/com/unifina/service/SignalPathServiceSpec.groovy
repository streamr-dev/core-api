package com.unifina.service

import com.unifina.BeanMockingSpecification
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecRole
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SecUserSecRole
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.Serialization
import com.unifina.exceptions.CanvasUnreachableException
import com.unifina.signalpath.RuntimeRequest
import com.unifina.signalpath.SignalPath
import com.unifina.signalpath.SignalPathRunner
import com.unifina.utils.Globals
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import java.security.AccessControlException

@TestFor(SignalPathService)
@Mock([SecUser, SecRole, SecUserSecRole, Canvas, Serialization])
class SignalPathServiceSpec extends BeanMockingSpecification {

	SecUser me
	SecUser admin
	Canvas c1
	CanvasService canvasService

	def setup() {
		me = new SecUser(username: "me@streamr.com", password: "pw", name: "name")
		me.save(failOnError: true)

		SecRole role = new SecRole(authority: "ROLE_ADMIN")
		role.save(failOnError: true)
		admin = new SecUser(username: "admin@streamr.com", password: "pw", name: "admin")
		admin.save(failOnError: true)
		new SecUserSecRole(secUser: admin, secRole: role).save(failOnError: true)

		c1 = new Canvas(
				name: "canvas-1",
				serialization: new Serialization(bytes: new byte[512], date: new Date()),
				runner: "runnerId"
		)
		c1.save(failOnError: true)
		assert c1.serialization.id != null
		canvasService = mockBean(CanvasService, Mock(CanvasService))
	}

	def "clearState() clears serialized state"() {
		when:
		service.clearState(c1)

		then:
		Canvas.findById(c1.id).serialization == null
	}

	def "stopLocalRunner() sets state to STOPPED if runner is not found"() {
		def service = Spy(SignalPathService)

		when:
		boolean success = service.stopLocalRunner("id")

		then:
		1 * service.updateState("id", Canvas.State.STOPPED) >> null
		!success
	}

	def "stopLocalRunner() tries to stop the runner if found and alive"() {
		def runner = Mock(SignalPathRunner)
		service.runnersById = [id: runner]

		when:
		boolean success = service.stopLocalRunner("id")

		then: "try to stop"
		1 * runner.abort()
		and: "stopped successfully"
		1 * runner.getRunning() >> false
		success
	}

	def "stopLocalRunner() returns false if the canvas doesn't stop"() {
		def runner = Mock(SignalPathRunner)
		service.runnersById = [id: runner]

		when:
		boolean success = service.stopLocalRunner("id")

		then: "try to stop"
		1 * runner.abort()
		and: "still running"
		1 * runner.getRunning() >> true
		!success
	}

	def "runtimeRequest() throws if stopping a canvas fails"() {
		def service = Spy(SignalPathService)
		def runner = Mock(SignalPathRunner)
		def sp = Mock(SignalPath)
		service.permissionService = Mock(PermissionService)
		service.runnersById[c1.runner] = runner

		when:
		service.runtimeRequest(new RuntimeRequest([type: 'stopRequest'], me, c1, "canvases/$c1.id", "canvases/$c1.id", new HashSet<>()))

		then:
		1 * runner.getSignalPath() >> sp
		1 * sp.getCanvas() >> c1
		1 * service.permissionService.check(me, c1, Permission.Operation.CANVAS_STARTSTOP) >> true
		1 * service.stopLocal(c1) >> false
		thrown(CanvasUnreachableException)
	}

	def "buildRuntimeRequest() must authorize and return a RuntimeRequest"() {
		when:
		RuntimeRequest req = service.buildRuntimeRequest([type: 'test'], "canvases/$c1.id", me)

		then:
		1 * canvasService.authorizedGetById(c1.id, me, Permission.Operation.CANVAS_GET) >> c1
		req.getType() == 'test'
		req.get("type") == 'test'
		req.getCheckedOperations().contains(Permission.Operation.CANVAS_GET)
		req.getPath() == "canvases/$c1.id"
		req.getOriginalPath() == req.getPath()
		req.getUser() == me
	}

	def "buildRuntimeRequest() lets admin role return a RuntimeRequest without permission"() {
		when:
		RuntimeRequest req = service.buildRuntimeRequest([type: 'test'], "canvases/$c1.id", admin)

		then:
		req.getType() == 'test'
		req.get("type") == 'test'
		req.getCheckedOperations().contains(Permission.Operation.CANVAS_GET)
		req.getPath() == "canvases/$c1.id"
		req.getOriginalPath() == req.getPath()
		req.getUser() == admin
	}

	def "buildRuntimeRequest() works in non-authenticated context (user is null)"() {
		when:
		RuntimeRequest req = service.buildRuntimeRequest([type: 'test'], "canvases/$c1.id", null)

		then:
		1 * canvasService.authorizedGetById(c1.id, null, Permission.Operation.CANVAS_GET) >> c1
		req.getType() == 'test'
		req.get("type") == 'test'
		req.getCheckedOperations().contains(Permission.Operation.CANVAS_GET)
		req.getPath() == "canvases/$c1.id"
		req.getOriginalPath() == req.getPath()
		req.getUser() == null
	}

	def "buildRuntimeRequest() must throw if the path is malformed"() {
		when:
		service.buildRuntimeRequest([type: 'test'], "foobar/$c1.id", me)

		then:
		thrown(IllegalArgumentException)
	}

	void "getUsersOfRunningCanvases() returns empty map if no canvases running"() {
		expect:
		service.getUsersOfRunningCanvases() == [:]
	}

	void "getUsersOfRunningCanvases() returns canvasId -> user mapping of running canvases"() {
		SecUser someoneElse = new SecUser(
			username: "someoneElse@streamr.com",
		).save(validate: false, failOnError: true)

		setup: "stub running canvases"
		Canvas c1 = new Canvas()
		Canvas c2 = new Canvas()
		Canvas c3 = new Canvas()

		c1.id = "canvas-1"
		c2.id = "canvas-2"
		c3.id = "canvas-3"

		SignalPath sp1 = new SignalPath()
		SignalPath sp2 = new SignalPath()
		SignalPath sp3 = new SignalPath()

		sp1.setCanvas(c1)
		sp2.setCanvas(c2)
		sp3.setCanvas(c3)

		service.runnersById = [
		    "runner-id-1": new SignalPathRunner(sp1, new Globals([:], someoneElse)),
			"runner-id-2": new SignalPathRunner(sp2, new Globals([:], me)),
			"runner-id-3": new SignalPathRunner(sp3, new Globals([:], someoneElse)),
		]

		expect:
		service.getUsersOfRunningCanvases() == [
		    "canvas-1": someoneElse,
			"canvas-2": me,
			"canvas-3": someoneElse
		]
	}

	void "getRunningSignalPaths() returns empty set if no SignalPath(s) running"() {
		expect:
		service.runningSignalPaths.isEmpty()
	}

	void "getRunningSignalPaths() returns list of running SignalPaths"() {
		setup: "stub running canvases"
		Canvas c1 = new Canvas()
		Canvas c2 = new Canvas()
		Canvas c3 = new Canvas()

		c1.id = "canvas-1"
		c2.id = "canvas-2"
		c3.id = "canvas-3"

		SignalPath sp1 = new SignalPath()
		SignalPath sp2 = new SignalPath()
		SignalPath sp3 = new SignalPath()

		sp1.setCanvas(c1)
		sp1.setName("sp1")
		sp2.setCanvas(c2)
		sp2.setName("sp2")
		sp3.setCanvas(c3)
		sp3.setName("sp3")

		service.runnersById = [
			"runner-id-1": new SignalPathRunner(sp1, new Globals([:], me)),
			"runner-id-2": new SignalPathRunner(sp2, new Globals([:], me)),
			"runner-id-3": new SignalPathRunner(sp3, new Globals([:], me)),
		]

		expect:
		service.runningSignalPaths.containsAll([sp1, sp2, sp3])
	}

	void "runtimeRequest() does not allow stopping canvas if user does not have write permission on canvas"() {
		Canvas canvas = new Canvas(runner: "runner-id")
		canvas.id = "canvas-id"

		SignalPath sp = new SignalPath()
		sp.canvas = canvas

		service.permissionService = new PermissionService()
		service.runnersById = [
			"runner-id": new SignalPathRunner(sp, new Globals()),
		]

		def request = new RuntimeRequest(
			[type: "stopRequest"],
			new SecUser(),
			canvas,
			"canvases/canvas-id",
			"",
			[] as Set
		)
		when:
		service.runtimeRequest(request, true)
		then:
		def e = thrown(AccessControlException)
		e.message == "stopRequest requires write permission!"
	}

	void "runtimeRequest() allows stopping canvas if user has write permission on canvas"() {
		SecUser user = new SecUser()
		user.save(failOnError: true, validate: false)

		Canvas canvas = new Canvas(runner: "runner-id")
		canvas.id = "canvas-id"

		SignalPath sp = new SignalPath()
		sp.canvas = canvas

		boolean isAborted = false

		def permissionService = service.permissionService = Mock(PermissionService)
		service.runnersById = [
			"runner-id": new SignalPathRunner(sp, new Globals()) {
				@Override
				void abort() {
					isAborted = true
				}
			},
		]

		def request = new RuntimeRequest(
			[type: "stopRequest"],
			user,
			canvas,
			"canvases/canvas-id",
			"",
			[] as Set
		)

		when:
		def response = service.runtimeRequest(request, true)

		then:
		noExceptionThrown()

		and:
		1 * permissionService.check(user, canvas, Permission.Operation.CANVAS_STARTSTOP) >> true

		and:
		response == [type: "stopRequest"]

		and:
		isAborted
	}

	void "runtimeRequest() allows stopping canvas if user is admin"() {
		SecUser user = new SecUser()
		user.save(failOnError: true, validate: false)

		SecRole adminRole = new SecRole(authority: "ROLE_ADMIN")
		adminRole.save(failOnError: true, validate: false)

		SecUserSecRole secUserSecRole = new SecUserSecRole(secUser: user, secRole: adminRole)
		secUserSecRole.save(failOnError: true, validate: false)

		Canvas canvas = new Canvas(runner: "runner-id")
		canvas.id = "canvas-id"

		SignalPath sp = new SignalPath()
		sp.canvas = canvas

		boolean isAborted = false

		service.permissionService = new PermissionService()
		service.runnersById = [
			"runner-id": new SignalPathRunner(sp, new Globals()) {
				@Override
				void abort() {
					isAborted = true
				}
			},
		]

		def request = new RuntimeRequest(
			[type: "stopRequest"],
			user,
			canvas,
			"canvases/canvas-id",
			"",
			[] as Set
		)

		when:
		def response = service.runtimeRequest(request, true)

		then:
		noExceptionThrown()

		and:
		response == [type: "stopRequest"]

		and:
		isAborted
	}

}
