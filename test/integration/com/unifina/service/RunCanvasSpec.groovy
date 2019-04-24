package com.unifina.service

import com.streamr.client.protocol.message_layer.StreamMessage
import com.streamr.client.protocol.message_layer.StreamMessageV30
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas

import grails.test.spock.IntegrationSpec
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

/**
 * Verifies that Canvases can be created, run, fed data through StreamService, and that the fed data be processed.
 */
class RunCanvasSpec extends IntegrationSpec {

	// Needed because otherwise a transaction started in the SignalPathRunner thread will deadlock with this one
	static transactional = false

	def static final SUM_FROM_1_TO_100_TIMES_2 = "10100.0"

	CanvasService canvasService
	SignalPathService signalPathService
	StreamService streamService

	SecUser user
	Stream stream

	def setup() {
		user = SecUser.load(1L)
		stream = Stream.get("run-canvas-spec")
	}

	def cleanup() {
		MessageHubFactory.stopAndClearAll() // Do not leave messagehub threads lying around
	}

	@Unroll
	def "start a canvas, send data to it via StreamService, and receive expected processed output values (#round)"(int round) {
		def conditions = new PollingConditions()
		Canvas canvas = Canvas.get("run-canvas-spec").refresh() // Make sure the state doesn't come from cache

		when:
		canvasService.start(canvas, true, user)

		// Allow time for Canvas to start
		Thread.sleep(5 * 1000)

		// Produce data
		(1..100).each {
			StreamMessage msg = new StreamMessageV30(stream.id, 0, System.currentTimeMillis(), 0L,
				"", "", null, 0L, StreamMessage.ContentType.CONTENT_TYPE_JSON,
				[numero: it, areWeDoneYet: false], StreamMessage.SignatureType.SIGNATURE_TYPE_NONE, null)
			streamService.sendMessage(msg)
		}

		// Terminator data package to know when we're done
		StreamMessage msg = new StreamMessageV30(stream.id, 0, System.currentTimeMillis(), 0L,
			"", "", null, 0L, StreamMessage.ContentType.CONTENT_TYPE_JSON,
			[numero: 0, areWeDoneYet: true], StreamMessage.SignatureType.SIGNATURE_TYPE_NONE, null)
		streamService.sendMessage(msg)

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
		canvasService.signalPathService.runnersById[canvas.runner].signalPaths[0].mods
	}
}
