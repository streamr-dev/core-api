package com.unifina.service

import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedFile
import com.unifina.domain.data.Stream
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.RunningSignalPath
import com.unifina.domain.task.Task
import com.unifina.feed.AbstractFeed
import com.unifina.feed.AbstractFeedProxy
import com.unifina.kafkaclient.UnifinaKafkaMessage
import com.unifina.kafkaclient.UnifinaKafkaProducer
import com.unifina.signalpath.AbstractSignalPathModule
import com.unifina.signalpath.SignalPath
import com.unifina.utils.CSVImporter
import com.unifina.utils.Globals
import com.unifina.utils.testutils.FakeMessageSource
import grails.converters.JSON
import grails.test.spock.IntegrationSpec
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import kafka.javaapi.consumer.SimpleConsumer
import org.apache.commons.logging.LogFactory
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsApplication

import java.nio.charset.Charset

class SignalPathServiceSpec extends IntegrationSpec {

	static final String SIGNAL_PATH_FILE = "signal-path-data.json"

	def log = LogFactory.getLog(getClass())


	def globals
	def grailsApplication
	def kafkaService
	def streamService
	def serializationService
	def signalPathService

	def user
	def stream

	def setup() {
		// A bit dirty, but we must do this because otherwise rsp.save() will be called in another thread
		// which causes exception related to transactions.
		signalPathService.metaClass.saveState = { SignalPath sp ->
			RunningSignalPath rsp = sp.runningSignalPath
			rsp.serialized = serializationService.serialize(sp)
		}

		kafkaService = new FakeKafkaService()

		signalPathService.servletContext = [:]
		signalPathService.kafkaService = kafkaService

		// Update feed 7 to use fake message source
		Feed feed = Feed.get(7L)
		feed.messageSourceClass = FakeMessageSource.canonicalName
		feed.save(failOnError: true)

		// Load user
		user = SecUser.load(1L)

		// Create stream
		stream = streamService.createUserStream([name: "serializationTestStream"], user)
		stream.streamConfig = (
			[
				fields: [
					[name: "a", type: "number"],
					[name: "b", type: "number"],
					[name: "c", type: "boolean"]
				],
				topic : stream.uuid
			] as JSON
		)
		stream.save(failOnError: true)
	}

	void "(de)serialization works correctly"() {
		def savedStructure = readSavedStructure(stream)
		def rsp = createAndRun(savedStructure, user)

		when: "running signal path and abruptly stopping and restarting"
		for (int i = 0; i < 100; ++i) {
			// Produce message to kafka
			kafkaService.sendMessage(stream, stream.uuid, [a: i, b: i * 2.5, c: i % 3 == 0])

			sleep(1000)

			// Log states of modules' outputs
			log.info(modules(rsp).collect { it.outputs.toString() }.join(" "))

			// On every 25th message stop and start running signal path
			if (i != 0 && i % 25 == 0) {
				signalPathService.stopLocal(rsp)
				signalPathService.startLocal(rsp, savedStructure["signalPathContext"])
				globals = kafkaService.globals = getGlobalsFrom(rsp)
				sleep(1000)
			}
		}

		// Collect values of outputs
		def actual = modules(rsp).collect {
			def h = [:]
			h[it.getName()] = it.outputs.collect { it.getValue() }
			h
		}

		// Stop running signal path
		signalPathService.stopLocal(rsp)

		then: "output values are as expected if no restarts had happened"
		actual == [[Stream: [99.0, 247.5, 1.0]], [Count: [100.0]], [Count: [100.0]], [Count: [100.0]], [Add: [300.0]]]
	}

	private def readSavedStructure(stream) {
		def s = new JsonSlurper().parseText(new File(getClass().getResource(SIGNAL_PATH_FILE).path).text)

		// Set correct values
		s.signalPathData.modules[0].params[0].value = stream.id
		s.signalPathContext.live = true

		s
	}

	private RunningSignalPath createAndRun(Map<String, Object> savedStructure, SecUser user) {
		def data = savedStructure["signalPathData"]
		RunningSignalPath rsp = signalPathService.createRunningSignalPath(data, user, false, true)

		signalPathService.startLocal(rsp, savedStructure["signalPathContext"])

		globals = kafkaService.globals = getGlobalsFrom(rsp)
		rsp
	}

	private Globals getGlobalsFrom(RunningSignalPath rsp) {
		signalPathService.servletContext["signalPathRunners"][rsp.runner].globals
	}

	private List<AbstractSignalPathModule> modules(RunningSignalPath rsp) {
		List<SignalPath> signalPaths = signalPathService.servletContext["signalPathRunners"][rsp.runner].signalPaths
		assert signalPaths.size() == 1
		signalPaths[0].mods
	}

	static class FakeKafkaService extends KafkaService {

		Globals globals

		@Override
		void createTopics(List<String> topics) {
		}

		@Override
		UnifinaKafkaProducer getProducer() {
			return new FakeUnifinaKafkaProducer()
		}

		@Override
		void sendMessage(Stream stream, Object key, Map message) {
			Collection<AbstractFeed> feeds = globals.getDataSource().getFeeds();
			if (feeds.size() != 1) {
				throw new RuntimeException("Feeds was of unexpected size " + feeds.size() + "!= 1")
			}
			AbstractFeedProxy feedProxy = (AbstractFeedProxy) feeds.iterator().next()
			FakeMessageSource source = (FakeMessageSource) feedProxy.hub.source

			source.handleMessage(new UnifinaKafkaMessage(
				stream.getUuid(),
				stream.getUuid(),
				System.currentTimeMillis(),
				UnifinaKafkaMessage.CONTENT_TYPE_JSON,
				new JsonBuilder(message).toString().getBytes("UTF-8")))
		}

		// Unsupported operations below

		@Override
		List<Task> createCollectTasks(Stream stream) {
			throw new UnsupportedOperationException()
		}

		@Override
		void sendMessage(String channelId, Object key, String message, boolean isJson) {
			throw new UnsupportedOperationException()
		}

		@Override
		void sendMessage(String channelId, Object key, String message) {
			throw new UnsupportedOperationException()
		}

		@Override
		void createTopics(List<String> topics, int partitions, int replicationFactor) {
			throw new UnsupportedOperationException()
		}

		@Override
		void createTopics(List<String> topics, int partitions) {
			throw new UnsupportedOperationException()
		}

		@Override
		List<FeedFile> createFeedFilesFromCsv(CSVImporter csv, Stream stream, FeedFileService feedFileService) {
			throw new UnsupportedOperationException()
		}

		@Override
		List<FeedFile> createFeedFilesFromCsv(CSVImporter csv, Stream stream) {
			throw new UnsupportedOperationException()
		}

		@Override
		GrailsApplication getGrailsApplication() {
			throw new UnsupportedOperationException()
		}

		@Override
		void setGrailsApplication(GrailsApplication grailsApplication) {
			throw new UnsupportedOperationException()
		}

		@Override
		void setProducer(UnifinaKafkaProducer producer) {
			throw new UnsupportedOperationException()
		}

		@Override
		void setTopicCreateConsumer(SimpleConsumer topicCreateConsumer) {
			throw new UnsupportedOperationException()
		}

		@Override
		void sendMessage(String channelId, Object key, Map message) {
			throw new UnsupportedOperationException()
		}

		@Override
		boolean topicExists(String topic) {
			throw new UnsupportedOperationException()
		}

		@Override
		void deleteTopics(List topics) {
			throw new UnsupportedOperationException()
		}

		@Override
		void createDeleteTopicTask(List topics, long delayMs) {
			throw new UnsupportedOperationException()
		}

		@Override
		Date getFirstTimestamp(String topic) {
			throw new UnsupportedOperationException()
		}
	}

	static class FakeUnifinaKafkaProducer extends UnifinaKafkaProducer {
		Logger log = Logger.getLogger(getClass())

		FakeUnifinaKafkaProducer() {
			super("", "")
		}

		@Override
		void sendRaw(String channel, String key, byte[] bytes) {
			String s = "Unexpected message %s (channel = %s, key = %s)"
			log.debug(String.format(s, new String(bytes, Charset.forName("UTF-8")), channel, key))
		}

		@Override
		protected void connect() {}

		// Unsupported operations below

		@Override
		void close() {
			throw new UnsupportedOperationException()
		}

		@Override
		void send(UnifinaKafkaMessage msg) {
			throw new UnsupportedOperationException()
		}

		@Override
		void sendString(String channel, String key, long timestamp, String msg) {
			throw new UnsupportedOperationException()
		}

		@Override
		void sendJSON(String channel, String key, long timestamp, String json) {
			throw new UnsupportedOperationException()
		}
	}
}
