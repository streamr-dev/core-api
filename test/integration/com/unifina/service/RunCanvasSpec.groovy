package com.unifina.service

import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.feed.FeedFactory
import grails.test.spock.IntegrationSpec
import spock.lang.Ignore
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

/**
 * Verifies that Canvases can be created, run, fed data through StreamService, and that the fed data be processed.
 */
@Ignore
class RunCanvasSpec extends IntegrationSpec {

	def static final SUM_FROM_1_TO_100_TIMES_2 = "10100.0"

	CanvasService canvasService
	SignalPathService signalPathService
	StreamService streamService

	SecUser user
	Stream stream

	def setupSpec() {
		Canvas.metaClass.'static'.executeUpdate = { String query, List list -> println "Invoking Fake ExecuteUpdate" }
	}

	def setup() {
		user = SecUser.load(1L)
		stream = Stream.get("run-canvas-spec")
		canvasService.signalPathService.servletContext = [signalPathRunners: [:]]
	}

	def cleanup() {
		FeedFactory.stopAndClearAll() // Do not leave messagehub threads lying around
	}

	@Unroll
	def "start a canvas, send data to it via StreamService, and receive expected processed output values (#round)"(int round) {
		def conditions = new PollingConditions()
		Canvas canvas = Canvas.get("run-canvas-spec")

		when:
		canvasService.start(canvas, true, user)

		Thread.sleep(2000)

		// Produce data
		(1..100).each { streamService.sendMessage(stream, [numero: it, areWeDoneYet: false], 30) }

		// Terminator data package to know when we're done
		streamService.sendMessage(stream, [numero: 0, areWeDoneYet: true], 30)

		// Synchronization: wait for terminator package
		conditions.within(10) { assert modules(canvasService, canvas)*.outputs[0][1].value == true }

		def finalState = modules(canvasService, canvas)*.outputs*.toString()

		then:
		finalState.size() == 4
		finalState[0] == "[(out) Stream.numero: 0.0, (out) Stream.areWeDoneYet: true]"
		finalState[1] == "[(out) Sum.out: $SUM_FROM_1_TO_100_TIMES_2]"
		finalState[2] == "[(out) Multiply.A*B: 0.0]"
		finalState[3] == "[(out) Constant.out: 2.0]"

		cleanup:
		signalPathService.stopLocal(canvas)

		where:
		round << (1..3)
	}

	static def modules(CanvasService canvasService, Canvas canvas) {
		canvasService.signalPathService.servletContext["signalPathRunners"][canvas.runner].signalPaths[0].mods
	}
}
