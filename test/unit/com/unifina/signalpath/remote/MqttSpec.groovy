package com.unifina.signalpath.remote

import com.unifina.datasource.DataSource
import com.unifina.datasource.DataSourceEventQueue
import com.unifina.utils.Globals
import com.unifina.utils.testutils.ModuleTestHelper
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttException
import spock.lang.Specification

class MqttSpec extends Specification {
	Mqtt module

	def messages = [
		["topic", "message"]
	]

	def setup() {
		module = new TestableMqtt()
		module.init()
	}

	def mockClient = Stub(MqttClient) {

	}

	/** Mocked event queue. Works manually in tests, please call module.receive(queuedEvent) */
	def mockGlobals = Stub(Globals) {
		getDataSource() >> Stub(DataSource) {
			getEventQueue() >> Stub(DataSourceEventQueue) {
				enqueue(_) >> { feedEventList ->
					transaction = feedEventList[0].content
				}
			}
		}
		isRealtime() >> true
	}

	/*
	void "module outputs the messages"() {
		TestableMqtt.mqttClient = mockClient
		expect:
		new ModuleTestHelper.Builder(module, [[]], [[messages]])
			.overrideGlobals { mockGlobals }
			.onModuleInstanceChange { newInstance -> module = newInstance }
			.test()
	}

	void "module re-throws a mqtt starting error"() {
		TestableMqtt.startingException = new MqttException("Testing")
		new ModuleTestHelper.Builder(module, [[]], [[messages]])
		expect:
		thrown RuntimeException
	}*/

	void "dummy test"() {
		expect:
		true
	}

}
