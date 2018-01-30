package com.unifina.signalpath.remote

import com.unifina.data.FeedEvent
import com.unifina.datasource.DataSource
import com.unifina.datasource.DataSourceEventQueue
import com.unifina.signalpath.Input
import com.unifina.signalpath.StringInput
import com.unifina.signalpath.StringParameter
import com.unifina.utils.Globals
import com.unifina.utils.testutils.ModuleTestHelper
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import spock.lang.Specification

class MqttSpec extends Specification {
	Mqtt module

	def setup() {
		module = new TestableMqtt()
		module.init()
	}

	def cleanup() {
		TestableMqtt.mqttClient = null
		TestableMqtt.startingException = null
	}

	def mockClient = Stub(MqttClient) {

	}

	/** Mocked event queue. Works manually in tests, please call module.receive(queuedEvent) */
	def mockGlobals = Stub(Globals) {
		getDataSource() >> Stub(DataSource) {
			enqueueEvent(_) >> { feedEvent ->
				event = feedEvent[0]
			}
		}
		isRealtime() >> true
	}
	FeedEvent event

	void "module outputs the messages"() {
		TestableMqtt.mqttClient = mockClient
		module.setGlobals(mockGlobals)

		def collector = new ModuleTestHelper.Collector()
		collector.init()
		collector.attachToOutput(module.outputs.find { it.name == "message" })

		String topic = "topic"
		String msg = "message"

		when:
		module.initialize()
		module.onStart()
		module.messageArrived(topic, new MqttMessage(msg.getBytes()))
		module.receive(event)
		module.onStop()

		then:
		collector.inputs[0].value == msg
	}

	void "module re-throws a mqtt starting error"() {
		TestableMqtt.startingException = new MqttException(new Exception("Testing"))
		module.setGlobals(mockGlobals)

		when:
		module.initialize()
		module.onStart()

		then:
		def e = thrown(RuntimeException)
		e.message.contains("Starting MQTT client failed")
	}

	void "test mqtt protocol is changed to tcp"() {
		when:
		Input urlInput = module.getInput("URL")
		urlInput.receive("mqtt://streamr.com/never/give/up/on/your/streams")

		then:
		TestableMqtt.getBrokerUrlFromInput(urlInput) == "tcp://streamr.com/never/give/up/on/your/streams"
	}

}
