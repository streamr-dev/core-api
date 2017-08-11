package com.unifina.signalpath.blockchain;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.unifina.datasource.ITimeListener;
import com.unifina.signalpath.*;
import com.unifina.utils.Globals;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.*;

/**
 * Get events sent out by given contract in the given transaction
 */
public class GetEvents extends AbstractSignalPathModule implements ITimeListener {
	private EthereumContractInput contract = new EthereumContractInput(this, "contract");

	// event -> [output for each event argument]
	private Map<EthereumABI.Event, List<Output<Object>>> events;

	private static final Logger log = Logger.getLogger(GetEvents.class);

	private transient Gson gson;

	private EthereumModuleOptions ethereumOptions = new EthereumModuleOptions();

	public static String GETH_URL = "http://localhost:8545";

	public String gethFilterId;

	// TODO: incorporate event argument parsing from web3j
	public StringOutput data = new StringOutput(this, "data");
	public ListOutput topics = new ListOutput(this, "topics");

	@Override
	public void initialize() {
		if (getGlobals().isRunContext()) {
			EthereumContract c = contract.getValue();
			if (c != null) {
				gethFilterId = startListeningContractEvents(c.getAddress());
			}
		}
	}

	@Override
	public void init() {
		propagationSink = true;
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

		JSONArray events = pollContractEvents(gethFilterId, (int)(time.getTime() % 0xfffffff));

		try {
			// TODO: parse event data&topics using stuff from web3j
//			for (int i = 0; i < events.length(); i++) {
//				JSONObject event = events.getJSONObject(i);
//			}
			if (events != null && events.length() > 0) {
				JSONArray topicsRaw = events.getJSONObject(0).getJSONArray("topics");
				List<String> topicsValues = new ArrayList<>();
				for (int i = 0; i < topicsRaw.length(); i++) {
					topicsValues.add(topicsRaw.getString(i));
				}
				topics.send(topicsValues);
				data.send(events.getJSONObject(0).getString("data"));
			}
		} catch (JSONException e) {

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

		/*
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
		*/

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
