package com.unifina.signalpath.blockchain;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.unifina.signalpath.*;
import com.unifina.signalpath.remote.AbstractHttpModule;
import com.unifina.utils.MapTraversal;
import grails.util.Holders;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Get events sent out by given contract in the given transaction
 */
public class GetEvents extends AbstractHttpModule {
	private EthereumContractInput contract = new EthereumContractInput(this, "contract");
	private StringInput txHash = new StringInput(this, "txHash");
	private ListOutput errors = new ListOutput(this, "errors");

	// event -> [output for each event argument]
	private Map<EthereumABI.Event, List<Output<Object>>> events;

	private static final Logger log = Logger.getLogger(GetEvents.class);

	private transient Gson gson;

	private EthereumModuleOptions ethereumOptions = new EthereumModuleOptions();

	/*
	@Override
	public void init() {
	}
	*/

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions.writeNetworkOption(options);

		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		EthereumContract c = contract.getValue();
		events = new HashMap<>();
		if (c != null) {
			EthereumABI abi = c.getABI();
			for (EthereumABI.Event ev : abi.getEvents()) {
				List<Output<Object>> evOutputs = new ArrayList<>();
				if (ev.inputs.size() > 0) {
					for (EthereumABI.Slot arg : ev.inputs) {
						String displayName = ev.name + "." + (arg.name.length() > 0 ? arg.name : "(" + arg.type + ")");
						Output output = EthereumToStreamrTypes.asOutput(arg.type, displayName, this);
						evOutputs.add(output);
						addOutput(output);
					}
				} else {
					// event without parameters/arguments/inputs
					Output output = new BooleanOutput(this, ev.name);
					evOutputs.add(output);
					addOutput(output);
				}
				events.put(ev, evOutputs);
			}
		}

		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions = EthereumModuleOptions.readFrom(options);
	}

	@Override
	protected HttpRequestBase createRequest() {
		EthereumContract c = contract.getValue();
		String URL = ethereumOptions.getServer() + "/events";
		String body = new Gson().toJson(ImmutableMap.of(
			"abi", c.getABI(),
			"address", c.getAddress(),
			"txHash", txHash.getValue()
		)).toString();
		HttpPost request = new HttpPost(URL);
		try {
			log.info("Sending getEvents call: " + body);
			request.setEntity(new StringEntity(body));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		return request;
	}

	public static class GetEventsResponse {
		// event name -> args in order they're in ABI
		public Map<String, List<JsonElement>> events;
	}

	@Override
	protected void sendOutput(HttpTransaction call) {
		try {
			HttpEntity entity = call.response.getEntity();
			String responseString = EntityUtils.toString(entity, "UTF-8");
			JsonObject responseJson = new JsonParser().parse(responseString).getAsJsonObject();
			if (responseJson.get("error") != null) {
				throw new RuntimeException(responseJson.get("error").toString());
			}
			if (gson == null) { gson = new Gson(); }
			GetEventsResponse response = gson.fromJson(responseString, GetEventsResponse.class);

			EthereumContract c = contract.getValue();
			for (EthereumABI.Event ev : c.getABI().getEvents()) {
				List<JsonElement> args = response.events.get(ev.name);
				List<Output<Object>> evOutputs = events.get(ev);
				if (args != null) {
					if (ev.inputs.size() > 0) {
						int n = Math.min(evOutputs.size(), args.size());
						for (int i = 0; i < n; i++) {
							String value = args.get(i).getAsString();
							Output output = evOutputs.get(i);
							EthereumCall.convertAndSend(output, value);
						}
					} else {
						evOutputs.get(0).send(Boolean.TRUE);
					}
				}
			}
		} catch (IllegalArgumentException | NullPointerException | IllegalStateException e) {
			log.error(e);
			call.errors.add("Empty or no response from server");
		} catch (Exception e) {
			call.errors.add(e.getMessage());
		}

		if (call.errors.size() > 0) {
			errors.send(call.errors);
		}
	}

	@Override
	public void clearState() {
		events = new HashMap<>();
	}
}
