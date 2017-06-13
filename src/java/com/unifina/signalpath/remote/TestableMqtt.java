package com.unifina.signalpath.remote;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * Holder for mock MQTT client.
 * @see TestableHttp for discussion on purpose of this class.
 */
public class TestableMqtt extends Mqtt {
	public transient static MqttClient mqttClient;
	public transient static MqttException startingException;

	@Override
	protected MqttClient createAndStartClient() throws MqttException {
		if (startingException != null) {
			throw startingException;
		}
		return mqttClient;
	}
}
