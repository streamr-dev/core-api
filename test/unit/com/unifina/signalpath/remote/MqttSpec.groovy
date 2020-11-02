package com.unifina.signalpath.remote


import com.unifina.data.Event
import com.unifina.datasource.DataSource
import com.unifina.signalpath.Input
import com.unifina.utils.Globals
import com.unifina.utils.testutils.ModuleTestHelper
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import spock.lang.Specification

class MqttSpec extends Specification {
	Mqtt module
	Globals mockGlobals

	def setup() {
		module = new TestableMqtt()
		module.init()

		/** Mocked event queue. Immediately dispatches events */
		mockGlobals = Stub(Globals) {
			getDataSource() >> Stub(DataSource) {
				enqueue(_ as Event) >> { Event e ->
					e.dispatch()
				}
			}
			isRealtime() >> true
		}
		mockGlobals.time = new Date()
	}

	def cleanup() {
		TestableMqtt.mqttClient = null
		TestableMqtt.startingException = null
	}

	def mockClient = Stub(MqttClient) {

	}

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
		urlInput.receive("mqtt://streamr.network/never/give/up/on/your/streams")

		then:
		TestableMqtt.getBrokerUrlFromInput(urlInput) == "tcp://streamr.network/never/give/up/on/your/streams"
	}

}
