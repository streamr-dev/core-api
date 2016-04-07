package com.unifina.signalpath.remote;

import com.unifina.signalpath.*;
import com.unifina.utils.MapTraversal;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Functionality that is common to HTTP modules
 */
public abstract class AbstractHttpModule extends AbstractSignalPathModule {

	// TODO: this probably should be enum or class?
	public static final String BODY_FORMAT_JSON = "text/json";
	public static final String BODY_FORMAT_FORMDATA = "application/x-www-form-urlencoded";
	public static final String BODY_FORMAT_PLAIN = "text/plain";
	public static final String BODY_FORMAT_XML = "application/xml";

	protected String bodyFormat = BODY_FORMAT_JSON;
	protected boolean trustSelfSigned = false;

	/** This function is overridden so that the tests can inject a mock HttpClient */
	protected HttpClient getHttpClient() {
		if (_httpClient == null) {
			if (trustSelfSigned) {
				try {
					SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
					_httpClient = HttpClients.custom().setSSLSocketFactory(new SSLConnectionSocketFactory(sslcontext)).build();
				} catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
					trustSelfSigned = false;
					// TODO: notify user that self-signed certificates aren't supported
				}
			}
			if (!trustSelfSigned) {
				_httpClient = HttpClients.createMinimal();
			}
		}
		return _httpClient;
	}
	private transient CloseableHttpClient _httpClient;

	@Override
	public Map<String,Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		ModuleOptions options = ModuleOptions.get(config);

		ModuleOption bodyFormatOption = new ModuleOption("bodyFormat", bodyFormat, ModuleOption.OPTION_STRING);
		bodyFormatOption.addPossibleValue("JSON", AbstractHttpModule.BODY_FORMAT_JSON);
		bodyFormatOption.addPossibleValue("Form-data", AbstractHttpModule.BODY_FORMAT_FORMDATA);
		options.add(bodyFormatOption);

		options.add(new ModuleOption("trustSelfSigned", trustSelfSigned, ModuleOption.OPTION_BOOLEAN));

		return config;
	}

	/** For bodyless verbs, "body" is only a "trigger" */
	@Override
	public void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		bodyFormat = MapTraversal.getString(config, "options.bodyFormat.value", AbstractHttpModule.BODY_FORMAT_JSON);
		trustSelfSigned = MapTraversal.getBoolean(config, "options.trustSelfSigned.value");

		// try getting HTTP client, resets trustSelfSigned if such SSL client can't be created
		getHttpClient();
	}

	@Override
	public void clearState() {}

	public static class VerbParameter extends StringParameter {
		public VerbParameter(AbstractSignalPathModule owner, String name) {
			super(owner, name, "POST"); //this.getValueList()[0]);
		}
		private List<PossibleValue> getValueList() {
			return Arrays.asList(
				new PossibleValue("GET", "GET"),
				new PossibleValue("POST", "POST"),
				new PossibleValue("PUT", "PUT"),
				new PossibleValue("DELETE", "DELETE"),
				new PossibleValue("PATCH", "PATCH")
			);
		}
		@Override public Map<String, Object> getConfiguration() {
			Map<String, Object> config = super.getConfiguration();
			config.put("possibleValues", getValueList());
			return config;
		}
		public boolean hasBody() {
			String v = this.getValue();
			return v.equals("POST") || v.equals("PUT") || v.equals("PATCH");
		}
		public HttpRequestBase getRequest(String url) {
			String v = this.getValue();
			return v.equals("GET") ? new HttpGet(url) :
				   	v.equals("POST") ? new HttpPost(url) :
					v.equals("PUT") ? new HttpPut(url) :
					v.equals("DELETE") ? new HttpDelete(url) :
					v.equals("PATCH") ? new HttpPatch(url) : new HttpPost(url);
		}
	}
}
