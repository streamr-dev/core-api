package com.unifina.signalpath.blockchain;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.unifina.datasource.ITimeListener;
import com.unifina.signalpath.*;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Get events sent out by given contract in the given transaction
 */
public class GetEvents extends AbstractSignalPathModule implements ITimeListener {
	private EthereumContractInput contract = new EthereumContractInput(this, "contract");
	private ListOutput errors = new ListOutput(this, "errors");

	// event -> [output for each event argument]
	private Map<EthereumABI.Event, List<Output<Object>>> events;

	private static final Logger log = Logger.getLogger(GetEvents.class);

	private transient Gson gson;

	private EthereumModuleOptions ethereumOptions = new EthereumModuleOptions();

	public static String GETH_URL = "http://localhost:8545";

	public String gethFilterId;

	@Override
	public void initialize() {
		if (getGlobals().isRunContext()) {
			EthereumContract c = contract.getValue();
			if (c != null) {
				gethFilterId = startListeningContractEvents(c.getAddress());
			} else {
				log.error("Contract input has no value in it");
			}
		}
	}

	@Override
	public void init() {
		propagationSink = true;
		addInput(contract);
		addOutput(errors);
	}

	/**
	 * Poll the geth filter
	 * @see "https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_getfilterchanges"
	 * @param time used as callId to RPC
     */
	@Override
	public void setTime(Date time) {
		// bad state; no need to spam logs, error was probably reported in initialize() already
		if (gethFilterId == null) { return; }

		EthereumContract c = contract.getValue();
		if (c == null) { return; }

		JSONArray events = pollContractEvents(gethFilterId, (int)(time.getTime() % 0xfffffff));

		// event appeared: now ask streamr-web3 to decode it
		// TODO: web3j should do the decoding; i.e. change to Java 8, or backport org.web3j.abi.FunctionReturnDecoder
		if (events != null && events.length() > 0) {
			try {
				String txHash = events.getJSONObject(0).getString("transactionHash");
				String url = ethereumOptions.getServer() + "/events";
				String body = new Gson().toJson(ImmutableMap.of(
					"abi", c.getABI().toString(),
					"address", c.getAddress(),
					"txHash", txHash
				));
				HttpResponse<JsonNode> response = Unirest.post(url)
					.header("Accept", "application/json")
					.header("Content-Type", "application/json")
					.body(body)
					.asJson();
				sendOutputs(response.getRawBody());
			} catch (JSONException e) {
				log.error("Error while parsing " + events.toString(), e);
			} catch (UnirestException e) {
				log.error("Error while decoding event (HTTP -> streamr-web3)", e);
			}
		}
	}

	private void sendOutputs(InputStream eventJsonStream) {
		// TODO: accumulate errors from earlier too
		List<String> errorList = new ArrayList<>();

		try {
			String responseString = IOUtils.toString(eventJsonStream, "UTF-8");
			JsonObject responseJson = new JsonParser().parse(responseString).getAsJsonObject();
			if (responseJson.get("error") != null) {
				throw new RuntimeException(responseJson.get("error").toString());
			}

			EthereumContract c = contract.getValue();
			for (EthereumABI.Event ev : c.getABI().getEvents()) {
				JsonArray args = responseJson.getAsJsonArray(ev.name);
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
		} catch (IOException e) {
			errorList.add(e.getMessage());
			log.error(e);
		}

		if (errorList.size() > 0) {
			errors.send(errorList);
		}
	}

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
	public void sendOutput() {

	}

	@Override
	public void clearState() {
		gethFilterId = null;
		events = new HashMap<>();
	}

	/**
	 * Send RPC call to geth
	 * @see "https://github.com/ethereum/wiki/wiki/JSON-RPC"
	 * @param params RPC params
	 * @return RPC return value
	 * @throws UnirestException if HTTP post fails
     */
	private static JSONObject rpcCall(String method, List params, Integer callId) throws UnirestException {
		HttpResponse<JsonNode> response = Unirest.post(GETH_URL).body(new Gson().toJson(ImmutableMap.of(
				"id", callId,
				"jsonrpc", "2.0",
				"method", method,
				"params", params
		))).asJson();
		return response.getBody().getObject();
	}

	private static String stringFromRpcCall(String method, List params) throws UnirestException, JSONException {
		return rpcCall(method, params, 123).getString("result");
	}
	private static Boolean booleanFromRpcCall(String method, List params) throws UnirestException, JSONException {
		return rpcCall(method, params, 123).getBoolean("result");
	}
	private static JSONArray arrayFromRpcCall(String method, List params) throws UnirestException, JSONException {
		return rpcCall(method, params, 123).getJSONArray("result");
	}
	private static JSONObject objectFromRpcCall(String method, List params) throws UnirestException, JSONException {
		return rpcCall(method, params, 123).getJSONObject("result");
	}
	private static String stringFromRpcCall(String method, List params, Integer callId) throws UnirestException, JSONException {
		return rpcCall(method, params, callId).getString("result");
	}
	private static JSONArray arrayFromRpcCall(String method, List params, Integer callId) throws UnirestException, JSONException {
		return rpcCall(method, params, callId).getJSONArray("result");
	}
	private static JSONObject objectFromRpcCall(String method, List params, Integer callId) throws UnirestException, JSONException {
		return rpcCall(method, params, callId).getJSONObject("result");
	}

	/**
	 * Register filter to geth: get events generated by contract in specified address
	 * @see "https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_newfilter"
	 * @param address of contract to listen
	 * @return filterId
     */
	public static String startListeningContractEvents(String address) {
		String response = null;
		try {
			response = stringFromRpcCall("eth_newFilter", Arrays.asList(ImmutableMap.of(
					"address", address
			)));
		} catch (Exception e) {
			log.error("Error while initializing / subscribing to geth", e);
		}

		return response;
	}

	public static boolean stopListeningContractEvents(String filterId) {
		Boolean response = null;
		try {
			response = booleanFromRpcCall("eth_uninstallFilter", Arrays.asList(filterId));
		} catch (Exception e) {
			log.error("Error while initializing / subscribing to geth", e);
		}

		return response;
	}

	public static JSONArray pollContractEvents(String filterId, Integer callId) {
		JSONArray response = null;
		try {
			response = arrayFromRpcCall("eth_getFilterChanges", Arrays.asList(filterId), callId);
		} catch (Exception e) {
			log.error("Error while initializing / subscribing to geth", e);
		}

		return response;
	}
}
