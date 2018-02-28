package com.unifina.service

import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.Serialization
import com.unifina.exceptions.CanvasUnreachableException
import com.unifina.signalpath.RuntimeRequest
import com.unifina.signalpath.SignalPath
import com.unifina.signalpath.SignalPathRunner
import com.unifina.utils.Globals
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(SignalPathService)
@Mock([SecUser, Canvas, Serialization])
class SignalPathServiceSpec extends Specification {

	SecUser me
	Canvas c1

	def setup() {
		me = new SecUser(username: "me@streamr.com", password: "pw", name: "name", timezone: "Europe/Helsinki")
		me.save(failOnError: true)

		c1 = new Canvas(
				name: "canvas-1",
				json: "{}",
				serialization: new Serialization(bytes: new byte[512], date: new Date()),
				runner: "runnerId"
		)
		c1.save(failOnError: true)
		assert c1.serialization.id != null

		service.canvasService = Mock(CanvasService)
		service.servletContext = [:]
	}

	def "clearState() clears serialized state"() {
		when:
		service.clearState(c1)

		then:
		Canvas.findById(c1.id).serialization == null
	}

	def "stopLocalRunner() sets state to STOPPED if runner is not found"() {
		def service = Spy(SignalPathService)
		service.servletContext = service.servletContext ?: [:]
		service.servletContext["signalPathRunners"] = [:]

		when:
		boolean success = service.stopLocalRunner("id")

		then:
		1 * service.updateState("id", Canvas.State.STOPPED) >> null
		!success
	}

	def "stopLocalRunner() tries to stop the runner if found and alive"() {
		def runner = Mock(SignalPathRunner)
		service.servletContext = service.servletContext ?: [:]
		service.servletContext.signalPathRunners = [id: runner]

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
		service.servletContext = service.servletContext ?: [:]
		service.servletContext.signalPathRunners = [id: runner]

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
		service.servletContext = service.servletContext ?: [:]
		service.servletContext.signalPathRunners = [:]
		service.servletContext.signalPathRunners[c1.runner] = runner

		when:
		service.runtimeRequest(new RuntimeRequest([type: 'stopRequest'], me, c1, "canvases/$c1.id", "canvases/$c1.id", new HashSet<>()))

		then:
		1 * runner.getSignalPaths() >> [sp]
		1 * sp.getCanvas() >> c1
		1 * service.permissionService.canWrite(me, c1) >> true
		1 * service.stopLocal(c1) >> false
		thrown(CanvasUnreachableException)
	}

	def "buildRuntimeRequest() must authorize and return a RuntimeRequest"() {
		when:
		RuntimeRequest req = service.buildRuntimeRequest([type: 'test'], "canvases/$c1.id", me)

		then:
		1 * service.canvasService.authorizedGetById(c1.id, me, Permission.Operation.READ) >> c1
		req.getType() == 'test'
		req.get("type") == 'test'
		req.getCheckedOperations().contains(Permission.Operation.READ)
		req.getPath() == "canvases/$c1.id"
		req.getOriginalPath() == req.getPath()
		req.getUser() == me
	}

	def "buildRuntimeRequest() must throw if the path is malformed"() {
		when:
		service.buildRuntimeRequest([type: 'test'], "foobar/$c1.id", me)

		then:
		thrown(IllegalArgumentException)
	}

	void "getUsersOfRunningCanvases() returns empty map if no canvases running"() {
		service.servletContext["signalPathRunners"] = [:]
		expect:
		service.getUsersOfRunningCanvases() == [:]
	}

	void "getUsersOfRunningCanvases() returns canvasId -> user mapping of running canvases"() {
		SecUser someoneElse = new SecUser(
			username: "someoneElse@streamr.com",
			timezone: "Europe/Helsinki"
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

		service.servletContext["signalPathRunners"] = [
		    "runner-id-1": new SignalPathRunner(sp1, new Globals([:], someoneElse), false),
			"runner-id-2": new SignalPathRunner(sp2, new Globals([:], me), false),
			"runner-id-3": new SignalPathRunner(sp3, new Globals([:], someoneElse), false),
		]

		expect:
		service.getUsersOfRunningCanvases() == [
		    "canvas-1": someoneElse,
			"canvas-2": me,
			"canvas-3": someoneElse
		]
	}

}
