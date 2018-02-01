package com.unifina.signalpath.remote;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;
import com.unifina.datasource.IStartListener;
import com.unifina.datasource.IStopListener;
import com.unifina.feed.ITimestamped;
import com.unifina.signalpath.*;

import com.unifina.utils.IdGenerator;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import org.apache.log4j.Logger;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayInputStream;
import java.util.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
import java.io.InputStream;
import java.security.cert.Certificate;

/**
 * Eclipse MqttClient wrapper
 */
public class Mqtt extends AbstractSignalPathModule implements MqttCallback, IEventRecipient, IStartListener, IStopListener {

	private static final Logger log = Logger.getLogger(Mqtt.class);

	private StringParameter URL = new StringParameter(this, "URL", "");
	private StringParameter topic = new StringParameter(this, "topic", "");
	private StringParameter username = new StringParameter(this, "username", "");
	private StringParameter password = new StringParameter(this, "password", "");
	private CertificateParameter certType = new CertificateParameter(this, "certType",CertificateParameter.NONE);
	private StringParameter certificate = new StringParameter(this, "certificate", "");

	private StringOutput message = new StringOutput(this, "message");

	private transient Propagator asyncPropagator;

	private transient MqttClient client;

	@Override
	public void init() {
		addInput(URL);
		addInput(topic);
		addInput(username);
		addInput(password);
		addInput(certType);
		addOutput(message);

		URL.setCanConnect(false);
		topic.setCanConnect(false);
		username.setCanConnect(false);
		password.setCanConnect(false);
		certificate.setCanConnect(false);
		certificate.setTextArea(true);

		certType.setUpdateOnChange(true);

		// sends output when messages arrive (though shouldn't receive inputs anyway...)
		setPropagationSink(true);
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		if (!certType.isNone()) {
			addInput(certificate);
		}
	}

	@Override
	public void initialize() {
		super.initialize();
		// copied from ModuleWithUI
		if (getGlobals().isRunContext()) {
			getGlobals().getDataSource().addStartListener(this);
			getGlobals().getDataSource().addStopListener(this);
		}
	}

	@Override
	public void onStart() {
		try {
			client = createAndStartClient();
			client.subscribe(topic.getValue());
		} catch (Exception e) {
			throw new RuntimeException("Starting MQTT client failed", e);
		}
	}

	@Override
	public void onStop() {
		try {
			stopClient();
		} catch (Exception e) {
			throw new RuntimeException("Stopping MQTT client failed", e);
		}
	}

	protected MqttClient createAndStartClient() throws MqttException {
		stopClient();

		String brokerUrl = getBrokerUrlFromInput(URL);
		String clientId = IdGenerator.getShort();
		MqttClient newClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
		newClient.setCallback(this);
		MqttConnectOptions connectOptions = new MqttConnectOptions();
		if (this.hasUsernameOrPassword()){
			connectOptions = addGivenUsernamePassword(connectOptions);
		}
		if (this.hasCertificate()){
			connectOptions = addGivenCertificate(connectOptions);
		}
		newClient.connect(connectOptions);
		return newClient;
	}

	private boolean hasUsernameOrPassword(){
		if (password.getValue().isEmpty() && username.getValue().isEmpty()){
			return false;
		}
		return true;
	}

	private boolean hasCertificate(){
		if (certType.getValue().equals(CertificateParameter.CRT)){
			return true;
		}
		return false;
	}

	private MqttConnectOptions addGivenUsernamePassword(MqttConnectOptions connectOptions) {
		connectOptions.setPassword(password.getValue().toCharArray());
		connectOptions.setUserName(username.getValue());
		return connectOptions;
	}

	private MqttConnectOptions addGivenCertificate(MqttConnectOptions connectOptions) {
		try {
			SSLContext context;
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			InputStream caInput = new ByteArrayInputStream(certificate.getValue().getBytes());
			Certificate ca;

			try {
				ca = cf.generateCertificate(caInput);
				String keyStoreType = KeyStore.getDefaultType();
				KeyStore keyStore = KeyStore.getInstance(keyStoreType);
				keyStore.load(null, null);
				keyStore.setCertificateEntry("ca", ca);

				String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
				TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
				tmf.init(keyStore);

				context =  SSLContext.getInstance("TLS");
				context.init(null, tmf.getTrustManagers(), null);
				SocketFactory factory = context.getSocketFactory();
				connectOptions.setSocketFactory(factory);
			} catch (Exception e){
				throw new RuntimeException("Certificate generation for MQTT client failed", e);
			}

		} catch (Exception e){
			throw new RuntimeException("SSL Context generation for MQTT client failed", e);
		}

		return connectOptions;
	}

	/**
	 * Replace "mqtt:" with "tcp:" which is what the PAHO MQTT library seems to require.
	 * The client won't recognize "mqtt:" protocol, yet many addresses are published like that
	 * @see MqttConnectOptions#validateURI
	 */
	public static String getBrokerUrlFromInput(Input<String> urlInput) {
		String brokerUrl = urlInput.getValue();
		if (brokerUrl.startsWith("mqtt:")) { brokerUrl = "tcp:" + brokerUrl.substring(5); }
		return brokerUrl;
	}

	protected void stopClient() throws MqttException {
		if (client != null) {
			client.disconnect();
			client = null;
		}
	}

	@Override
	public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
		final MqttMessageEvent event = new MqttMessageEvent(getGlobals().time);
		event.message = mqttMessage;
		// push mqtt message into FeedEvent queue; it will later call this.receive
		getGlobals().getDataSource().enqueueEvent(new FeedEvent<>(event, event.timestamp, this));
	}

	@Override
	public void receive(FeedEvent event) {
		if (event.content instanceof MqttMessageEvent) {
			sendOutput(((MqttMessageEvent) event.content).message);
			getPropagator().propagate();
		} else {
			super.receive(event);
		}
	}

	private Propagator getPropagator() {
		if (asyncPropagator == null) {
			asyncPropagator = new Propagator(this);
		}
		return asyncPropagator;
	}

	public void sendOutput(MqttMessage msg) {
		message.send(new String(msg.getPayload()));
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
		// not called; we're not sending anything
	}

	@Override
	public void connectionLost(Throwable error) {
		log.error(error);
	}

	@Override
	public void sendOutput() {
		// "normal" activation time does nothing; only received MQTT messages cause propagation
		// TODO: maybe allow topic input and change subscribed topic if topic input is activated?
	}

	@Override
	public void clearState() {

	}

	private static class MqttMessageEvent implements ITimestamped {
		public Date timestamp;
		public MqttMessage message;

		public MqttMessageEvent(Date timestamp) {
			this.timestamp = timestamp;
		}

		@Override
		public Date getTimestamp() {
			return timestamp;
		}
	}

	public static class CertificateParameter extends StringParameter {

		public static final String NONE = "none";
		public static final String CRT = ".crt";

		public CertificateParameter(AbstractSignalPathModule owner, String name, String defaultValue) {
			super(owner, name, defaultValue);
		}

		@Override
		protected List<PossibleValue> getPossibleValues() {
			return Arrays.asList(
				new PossibleValue("none", NONE),
				new PossibleValue(".crt", CRT)
			);
		}

		public boolean isNone() { return getValue().equals(NONE);}
	}
}
