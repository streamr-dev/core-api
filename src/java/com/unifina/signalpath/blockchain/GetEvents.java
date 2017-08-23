package com.unifina.signalpath.blockchain;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.unifina.datasource.ITimeListener;
import com.unifina.signalpath.*;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Get events sent out by given contract in the given transaction
 */
public class GetEvents extends AbstractSignalPathModule implements ITimeListener {
	private static final Logger log = Logger.getLogger(GetEvents.class);

	private final EthereumContractInput contract = new EthereumContractInput(this, "contract");
	private final ListOutput errors = new ListOutput(this, "errors");
	private Map<String, List<Output>> outputsByEvent; // event name -> [output for each event argument]

	private EthereumModuleOptions ethereumOptions = new EthereumModuleOptions();
	private transient ContractEventPoller contractEventPoller;

	@Override
	public void init() {
		setPropagationSink(true);
		addInput(contract);
		addOutput(errors);
	}

	@Override
	public void initialize() {
		if (getGlobals().isRunContext()) {
			if (contract.hasValue()) {
				String rpcUrl = ethereumOptions.getRpcUrl();
				String contractAddress = contract.getValue().getAddress();
				contractEventPoller = new ContractEventPoller(rpcUrl, contractAddress);
			} else {
				throw new RuntimeException("Contract input must have a value.");
			}
		}
	}

	@Override
	public void setTime(Date time) {
		List<String> errorList = new ArrayList<>();
		try {
			JSONArray events = contractEventPoller.poll((int) (time.getTime() % 0xfffffff));

			// Event appeared: now ask Streamr-Web3 to decode it and then send values to outputs
			if (events != null && events.length() > 0) {
				String txHash = events.getJSONObject(0).getString("transactionHash");
				JsonObject decodedEvent = fetchDecodedEvent(txHash);

				if (decodedEvent.get("error") != null) {
					errorList.add(decodedEvent.get("error").toString());
				} else {
					sendEventOutputs(decodedEvent);
				}
			}
		} catch (JSONException e) {
			errorList.add("JSON error: " + e);
			log.error("JSON error: " + e);
		} catch (UnirestException e) {
			errorList.add("Error while decoding event (HTTP -> streamr-web3): " + e);
			log.error("Error while decoding event (HTTP -> streamr-web3)", e);
		} catch (IOException e) {
			errorList.add("Error parsing response " + e);
			log.error("Error parsing response", e);
		} catch (EthereumJsonRpc.Error e) {
			errorList.add(e.getMessage());
			log.error(e.getMessage());
		}

		if (!errorList.isEmpty()) {
			errors.send(errorList);
		}
	}

	// TODO: web3j should do the decoding; i.e. change to Java 8, or backport org.web3j.abi.FunctionReturnDecoder
	private JsonObject fetchDecodedEvent(String txHash) throws UnirestException, IOException {
		String url = ethereumOptions.getServer() + "/events";
		String body = new Gson().toJson(ImmutableMap.of(
			"abi", contract.getValue().getABI().toString(),
			"address", contract.getValue().getAddress(),
			"txHash", txHash
		));
		InputStream eventJsonStream = Unirest.post(url)
			.header("Accept", "application/json")
			.header("Content-Type", "application/json")
			.body(body)
			.asJson()
			.getRawBody();

		String responseString = IOUtils.toString(eventJsonStream, "UTF-8");
		return new JsonParser().parse(responseString).getAsJsonObject();
	}

	private void sendEventOutputs(JsonObject decodedEvent) {
		for (EthereumABI.Event abiEvent : contract.getValue().getABI().getEvents()) {
			JsonArray values = decodedEvent.getAsJsonArray(abiEvent.name);
			if (values != null) {
				List<Output> eventOutputs = outputsByEvent.get(abiEvent.name);
				if (abiEvent.inputs.size() > 0) {
					int n = Math.min(eventOutputs.size(), values.size());
					for (int i = 0; i < n; i++) {
						String value = values.get(i).getAsString();
						Output output = eventOutputs.get(i);
						EthereumCall.convertAndSend(output, value);
					}
				} else {
					eventOutputs.get(0).send(Boolean.TRUE);
				}
			}
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

		outputsByEvent = new HashMap<>();

		if (contract.hasValue()) {
			EthereumABI abi = contract.getValue().getABI();
			for (EthereumABI.Event abiEvent : abi.getEvents()) {
				List<Output> eventOutputs = new ArrayList<>();
				if (abiEvent.inputs.size() > 0) {
					for (EthereumABI.Slot arg : abiEvent.inputs) {
						String displayName = abiEvent.name + "." + (arg.name.length() > 0 ? arg.name : "(" + arg.type + ")");
						Output output = EthereumToStreamrTypes.asOutput(arg.type, displayName, this);
						eventOutputs.add(output);
						addOutput(output);
					}
				} else {
					// event without parameters/arguments/inputs
					Output output = new BooleanOutput(this, abiEvent.name);
					eventOutputs.add(output);
					addOutput(output);
				}
				outputsByEvent.put(abiEvent.name, eventOutputs);
			}
		}

		ModuleOptions options = ModuleOptions.get(config);
		ethereumOptions = EthereumModuleOptions.readFrom(options);
	}

	@Override
	public void sendOutput() {}

	@Override
	public void clearState() {}

	@Override
	public void destroy() {
		super.destroy();
		contractEventPoller.close();
	}
}
