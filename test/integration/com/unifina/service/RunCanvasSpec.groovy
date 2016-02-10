package com.unifina.service

import com.unifina.api.SaveCanvasCommand
import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.feed.FeedFactory
import com.unifina.utils.IdGenerator
import grails.test.spock.IntegrationSpec
import groovy.json.JsonBuilder

import static com.unifina.TestHelper.*
import static com.unifina.service.CanvasTestHelper.*

/**
 * Verifies that Canvases can be created, run, fed data through Kafka, and that the fed data be processed.
 */
class RunCanvasSpec extends IntegrationSpec {

	def static final SUM_FROM_1_TO_100_TIMES_2 = "10100.0"

	static final String MODULES_LIST_FILE = "modules.json"

	def canvasService
	def kafkaService

	Canvas canvas
	SecUser user
	Stream stream

	def setup() {
		hackServiceForTestFriendliness(canvasService.signalPathService)

		user = SecUser.load(1L)

		// Create stream
		String uuid = IdGenerator.get()
		stream = new Stream(
			name: "run-canvas-spec-stream",
			feed: Feed.load(7L),
			user: user,
			description: "Data stream for ${RunCanvasSpec.name}",
			apiKey: "apiKey3185",
			uuid: uuid,
			config: new JsonBuilder([
			    topic: uuid,
				fields: [
				    [name: "numero", type: "number"],
					[name: "areWeDoneYet", type: "boolean"]
				]
			]).toString(),
		).save(failOnError: true)

		// Read modules from file
		def modules = readCanvasJsonAndReplaceStreamId(getClass(), MODULES_LIST_FILE, stream).modules

		// Create new canvas
		SaveCanvasCommand command = new SaveCanvasCommand(
			name: "canvas-test",
			modules: modules,
			settings: [
				adhoc: true,
			    live: true,
				speed: "0"
			]
		)
		canvas = canvasService.createNew(command, user)
	}

	def cleanup() {
		FeedFactory.stopAndClearAll() // Do not leave messagehub threads lying around
	}

	def "should be able to start a canvas, send data to it via Kafka, and received expected processed output values"() {
		when:
		// Start canvas
		canvasService.start(canvas, true)

		// Produce data
		(1..100).each {
			kafkaService.sendMessage(stream, stream.uuid, [numero: it, areWeDoneYet: false])

			// Synchronization
			waitFor(true) {
				modules(canvasService, canvas)[0].outputs[0].value == it
			}
		}

		// Last data package with areWeDone indicator set to true
		kafkaService.sendMessage(stream, stream.uuid, [numero: 0, areWeDoneYet: true])

		// Synchronization
		def actual = null
		waitFor(true) {
			actual = modules(canvasService, canvas)*.outputs*.toString()
			return (modules(canvasService, canvas)*.outputs[0][1].previousValue == 1.0)
		}

		then:
		actual.size() == 4
		actual[0] == "[(out) Stream.numero: 0.0, (out) Stream.areWeDoneYet: 1.0]"
		actual[1] == "[(out) Sum.out: $SUM_FROM_1_TO_100_TIMES_2]"
		actual[2] == "[(out) Multiply.A*B: 0.0]"
		actual[3] == "[(out) Constant.out: null]"

		cleanup:
		canvasService.stop(canvas, user)
	}
}
