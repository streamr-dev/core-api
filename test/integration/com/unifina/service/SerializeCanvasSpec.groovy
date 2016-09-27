package com.unifina.service

import com.unifina.api.SaveCanvasCommand
import com.unifina.data.StreamrBinaryMessage
import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.task.Task
import com.unifina.feed.AbstractFeed
import com.unifina.feed.AbstractFeedProxy
import com.unifina.kafkaclient.UnifinaKafkaMessage
import com.unifina.kafkaclient.UnifinaKafkaProducer
import com.unifina.utils.CSVImporter
import com.unifina.utils.Globals
import com.unifina.utils.testutils.FakeMessageSource
import grails.converters.JSON
import grails.test.spock.IntegrationSpec
import groovy.json.JsonBuilder
import kafka.javaapi.consumer.SimpleConsumer
import org.apache.commons.logging.LogFactory
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication
import spock.util.concurrent.PollingConditions

import java.nio.charset.Charset

import static com.unifina.service.CanvasTestHelper.*

class SerializeCanvasSpec extends IntegrationSpec {

	static final String SIGNAL_PATH_FILE = "signal-path-data.json"

	def log = LogFactory.getLog(getClass())


	def globals
	def grailsApplication
	def canvasService
	def kafkaService
	def streamService
	def serializationService
	def signalPathService

	SecUser user
	Stream stream

	KafkaProducer mockProducer

	def setup() {
		// Update feed 7 to use fake message source in place of real one
		Feed feed = Feed.get(7L)
		feed.messageSourceClass = FakeMessageSource.canonicalName
		feed.save(failOnError: true)

		hackServiceForTestFriendliness(signalPathService)

		// Wire up classes
		mockProducer = Mock(KafkaProducer)
		kafkaService = new FakeKafkaService(mockProducer)
		signalPathService.kafkaService = kafkaService
		canvasService.signalPathService = signalPathService

		// Load user
		user = SecUser.load(1L)

		// Create stream
		stream = streamService.createStream([name: "serializationTestStream", feed: 7L], user)
		stream.config = (
			[
				fields: [
					[name: "a", type: "number"],
					[name: "b", type: "number"],
					[name: "c", type: "boolean"]
				],
				topic : stream.id
			] as JSON
		)
		stream.save(failOnError: true)
	}

	void "(de)serialization works correctly"() {
		def conditions = new PollingConditions()
		def savedStructure = readCanvasJsonAndReplaceStreamId(getClass(), SIGNAL_PATH_FILE, stream)
		def canvas = createAndRun(savedStructure)

		when: "running Canvas and abruptly stopping and restarting"
		for (int i = 0; i < 100; ++i) {

			// Produce message to kafka
			kafkaService.sendMessage(stream, stream.id, [a: i, b: i * 2.5, c: i % 3 == 0])
			sleep(25)

			// Synchronize with thread
			conditions.within(10) { assert modules(canvasService, canvas)[3].inputs.find { it.name == "in" }.value == i }

			// Log states of modules' outputs
			log.info(modules(canvasService, canvas)*.outputs*.toString().join(" "))

			// On every 25th message stop and start Canvas
			if (i != 0 && i % 25 == 0) {
				sleep(300 + serializationService.serializationIntervalInMillis())
				canvasService.stop(canvas, user)
				canvas.state = Canvas.State.STOPPED
				canvasService.start(canvas, false)
				globals = kafkaService.globals = globals(canvasService, canvas)
				sleep(1000) // msgs might be sent before the canvas is really running
			}
		}

		// Collect values of outputs
		def actual = modules(canvasService, canvas).collect {
			def h = [:]
			h[it.getName()] = it.outputs.collect { it.getValue() }
			h
		}

		then: "output values are as expected if no restarts had happened"
		actual == [[Stream: [99.0, 247.5, 1.0]], [Count: [100.0]], [Count: [100.0]], [Count: [100.0]], ["Add (old)": [300.0]]]

		cleanup:
		if (canvas.state == Canvas.State.RUNNING) {
			canvasService.stop(canvas, user)
		}
	}

	private Canvas createAndRun(savedStructure) {
		SaveCanvasCommand command = new SaveCanvasCommand(savedStructure)
		Canvas c = canvasService.createNew(command, user)
		canvasService.start(c, true)
		globals = kafkaService.globals = globals(canvasService, c)
		return c
	}

	static class FakeKafkaService extends KafkaService {

		Globals globals

		KafkaProducer mockProducer

		public FakeKafkaService(KafkaProducer mockProducer) {
			this.mockProducer = mockProducer
		}

		@Override
		KafkaProducer<String, byte[]> getProducer() {
			return mockProducer
		}

		@Override
		void sendMessage(Stream stream, String partitionKey=null, Map message, int ttl=0) {
			Collection<AbstractFeed> feeds = globals.getDataSource().getFeeds();
			if (feeds.size() != 1) {
				throw new RuntimeException("Feeds was of unexpected size " + feeds.size() + "!= 1")
			}
			AbstractFeedProxy feedProxy = (AbstractFeedProxy) feeds.iterator().next()
			FakeMessageSource source = (FakeMessageSource) feedProxy.hub.source

			source.handleMessage(new StreamrBinaryMessage(
				stream.getId(), 0,
				System.currentTimeMillis(),
				StreamrBinaryMessage.CONTENT_TYPE_JSON,
				new JsonBuilder(message).toString().getBytes("UTF-8"),
				0))
		}

	}

}
