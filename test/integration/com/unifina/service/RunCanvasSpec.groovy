package com.unifina.service

import com.unifina.api.SaveCanvasCommand
import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.feed.FeedFactory
import grails.test.spock.IntegrationSpec
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

import static com.unifina.service.CanvasTestHelper.*

/**
 * Verifies that Canvases can be created, run, fed data through Kafka, and that the fed data be processed.
 */
class RunCanvasSpec extends IntegrationSpec {

	def static final SUM_FROM_1_TO_100_TIMES_2 = "10100.0"

	static final String MODULES_LIST_FILE = "modules.json"

	def canvasService
	def kafkaService
	def streamService

	SecUser user
	Stream stream
	SaveCanvasCommand saveCanvasCommand

	def setup() {
		hackServiceForTestFriendliness(canvasService.signalPathService)

		user = SecUser.load(1L)

		// Create stream
		stream = streamService.createStream([
				name: "run-canvas-spec-stream",
				feed: Feed.load(7L),
				description: "Data stream for ${RunCanvasSpec.name}",
				config: '{"fields": [{"name": "numero", "type": "number"}, {"name": "areWeDoneYet", "type": "boolean"}]}'
			], user)


		// Read modules from file
		def modules = readCanvasJsonAndReplaceStreamId(getClass(), MODULES_LIST_FILE, stream).modules

		// Create new canvas
		saveCanvasCommand = new SaveCanvasCommand(
			name: "canvas-test",
			modules: modules,
			settings: [
				adhoc: true,
			    live: true,
				speed: "0"
			]
		)
	}

	def cleanup() {
		streamService.deleteStream(stream)
		FeedFactory.stopAndClearAll() // Do not leave messagehub threads lying around
	}

	@Unroll
	def "start a canvas, send data to it via Kafka, and receive expected processed output values (#round)"(int round) {
		def conditions = new PollingConditions()
		Canvas canvas = canvasService.createNew(saveCanvasCommand, user)

		when:
		canvasService.start(canvas, true)

		// Produce data
		(1..100).each { kafkaService.sendMessage(stream, stream.id, [numero: it, areWeDoneYet: false]) }

		// Terminator data package to know when we're done
		kafkaService.sendMessage(stream, stream.id, [numero: 0, areWeDoneYet: true])

		// Synchronization: wait for terminator package
		conditions.within(10) { assert modules(canvasService, canvas)*.outputs[0][1].previousValue == 1.0 }

		def finalState = modules(canvasService, canvas)*.outputs*.toString()

		then:
		finalState.size() == 4
		finalState[0] == "[(out) Stream.numero: 0.0, (out) Stream.areWeDoneYet: 1.0]"
		finalState[1] == "[(out) Sum.out: $SUM_FROM_1_TO_100_TIMES_2]"
		finalState[2] == "[(out) Multiply.A*B: 0.0]"
		finalState[3] == "[(out) Constant.out: 2.0]"

		cleanup:
		canvasService.stop(canvas, user)

		where:
		round << (1..3)
	}
}
