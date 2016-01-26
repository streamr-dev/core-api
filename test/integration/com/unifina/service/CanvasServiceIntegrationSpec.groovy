package com.unifina.service

import com.unifina.api.SaveCanvasCommand
import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.utils.IdGenerator
import grails.test.spock.IntegrationSpec
import groovy.json.JsonSlurper
import org.apache.commons.logging.LogFactory

class CanvasServiceIntegrationSpec extends IntegrationSpec {

	static final String MODULES_LIST_FILE = "modules.json"

	def log = LogFactory.getLog(getClass())

	def canvasService
	def kafkaService
	def streamService

	Canvas canvas
	SecUser user
	Stream stream

	def setup() {
		canvasService.signalPathService.servletContext = [:]
		user = SecUser.load(1L)

		String uuid = IdGenerator.get()
		stream = new Stream(
			name: "stream-test",
			feed: Feed.load(7L),
			user: user,
			description: "Data stream for ${CanvasServiceIntegrationSpec.name}",
			apiKey: "apiKey3185",
			uuid: uuid,
			config: '{"topic":"' + uuid + '","fields":[{"name":"numero","type":"number"},{"name":"sana","type":"string"}]}'
		).save(failOnError: true)

		String txt = new File(getClass().getResource(MODULES_LIST_FILE).path).text
		txt = txt.replaceAll("STREAM_ID", stream.id.toString())
		def modules = new JsonSlurper().parseText(txt).modules

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

	def "todo"() {
		when:
		canvasService.start(canvas, false)
		sleep(5000)

		(1..100).each {
			kafkaService.sendMessage(stream, 0, [numero: it.intValue(), sana: "ebin"])
			sleep(500)
			log.info(modules().collect { it.outputs.toString() }.join(" "))
		}

		then:
		true

	}

	def modules() {
		canvasService.signalPathService.servletContext["signalPathRunners"][canvas.runner].signalPaths[0].mods
	}
}
