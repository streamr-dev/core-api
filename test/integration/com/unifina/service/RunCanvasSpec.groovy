package com.unifina.service

import com.unifina.api.SaveCanvasCommand
import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.utils.IdGenerator
import grails.test.spock.IntegrationSpec
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.apache.commons.logging.LogFactory

/**
 * Verifies that Canvases can be created, run, fed data through Kafka, and that the fed data be processed.
 */
class RunCanvasSpec extends IntegrationSpec {

	def static final SUM_FROM_1_TO_100_TIMES_2 = "10100.0"

	static final String MODULES_LIST_FILE = "modules.json"

	def log = LogFactory.getLog(getClass())

	def canvasService
	def kafkaService

	Canvas canvas
	SecUser user
	Stream stream

	def setup() {
		canvasService.signalPathService.servletContext = [:]

		user = SecUser.load(1L)

		// Create stream
		String uuid = IdGenerator.get()
		stream = new Stream(
			name: "stream-test",
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
		String txt = new File(getClass().getResource(MODULES_LIST_FILE).path).text
		txt = txt.replaceAll("STREAM_ID", stream.id.toString())
		def modules = new JsonSlurper().parseText(txt).modules

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

	def "should be able to start a canvas, send data to it via Kafka, and received expected processed output values"() {
		when:
		// Start canvas
		canvasService.start(canvas, false)

		sleep(500)

		// Produce data
		(1..100).each {
			kafkaService.sendMessage(stream, stream.uuid, [numero: it.intValue(), areWeDoneYet: false])
			sleep(10)
		}

		// Last data package with areWeDone indicator set to true
		kafkaService.sendMessage(stream, stream.uuid, [numero: 0, areWeDoneYet: true])

		def actual = null
		for (int i=0; i < 500; ++i) {
			actual = modules()*.outputs*.toString()

			boolean areWeDoneYet = modules()*.outputs[0][1].previousValue == 1.0
			if (areWeDoneYet) {
				break
			} else if (i < 499) {
				sleep(100)
			} else {
				throw new RuntimeException("Stream not updated in expected time!")
			}
		}


		then:
		actual.size() == 4
		actual[0] == "[(out) Stream.numero: 0.0, (out) Stream.areWeDoneYet: 1.0]"
		actual[1] == "[(out) Sum.out: $SUM_FROM_1_TO_100_TIMES_2]"
		actual[2] == "[(out) Multiply.A*B: 0.0]"
		actual[3] == "[(out) Constant.out: null]"

	}

	def modules() {
		canvasService.signalPathService.servletContext["signalPathRunners"][canvas.runner].signalPaths[0].mods
	}

	def globals() {
		canvasService.signalPathService.servletContext["signalPathRunners"][canvas.runner].globals
	}
}
