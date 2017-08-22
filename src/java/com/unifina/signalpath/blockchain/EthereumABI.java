package com.unifina.signalpath.blockchain;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.unifina.signalpath.Output;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EthereumABI implements Serializable {

	private static final Logger log = Logger.getLogger(EthereumABI.class);

	private Function constructor = null;
	private List<Function> functions = new ArrayList<>();

	private List<Event> events = new ArrayList<>();
	private final String json;

	public EthereumABI(List<Map<String, Object>> abiAsMap) {
		this(abiAsMap == null ? null : new Gson().toJsonTree(abiAsMap).getAsJsonArray());
	}

	public EthereumABI(String abiAsJson) {
		this(abiAsJson == null ? null : new JsonParser().parse(abiAsJson).getAsJsonArray());
	}

	public EthereumABI(JsonArray jsonArray) {
		if (jsonArray == null) {
			this.json = "[]";
			return;
		}

		Gson gson = new Gson();
		this.json = gson.toJson(jsonArray);

		log.debug("Parsing interface: " + jsonArray);

		for (JsonElement element : jsonArray) {
			JsonObject functionOrEvent = element.getAsJsonObject();
			String type = functionOrEvent.get("type").getAsString();

			// from https://github.com/ethereum/wiki/wiki/Ethereum-Contract-ABI#json: "type can be omitted, defaulting to function"
			if (type == null) {
				type = "function";
			}

			// fallback is the unnamed function that gets called when contract receives a
			//   transaction without specified function call (e.g. transaction with only ether)
			//   it has no inputs or outputs, so it gets EthereumABI.Function default values except for payable
			if (type.equals("function") || type.equals("fallback")) {
				EthereumABI.Function f = gson.fromJson(functionOrEvent, EthereumABI.Function.class);
				log.debug("Found function " + f.name + (f.constant ? " [constant]" : "") + (f.payable ? " [payable]" : ""));
				functions.add(f);
			} else if (type.equals("constructor")) {
				EthereumABI.Function f = gson.fromJson(functionOrEvent, EthereumABI.Function.class);
				log.debug("Found constructor " + f.name + (f.constant ? " [constant]" : "") + (f.payable ? " [payable]" : ""));
				constructor = f;
			} else if (type.equals("event")) {
				EthereumABI.Event e = gson.fromJson(functionOrEvent, EthereumABI.Event.class);
				log.debug("Found event " + e.name);
				events.add(e);
			} else {
				throw new RuntimeException("Whoa! Found unknown item type in ABI: "+type);
			}
		}
	}

	public Function getConstructor() {
		return constructor;
	}

	public List<Function> getFunctions() {
		return functions;
	}

	public List<Event> getEvents() {
		return events;
	}

	public String toString() {
		return json;
	}

	public List<Map<String, Object>> toList() {
		Type type = new TypeToken<List<Map<String, Object>>>(){}.getType();
		return new Gson().fromJson(json, type);
	}

	/** Ethereum contract input/output */
	public static class Slot implements Serializable {
		public String name;
		public String type;
	}

	/** Ethereum contract member function */
	public static class Function implements Serializable {
		public String name = "";
		public List<Slot> inputs = Collections.emptyList();
		public List<Slot> outputs = Collections.emptyList();
		public Boolean payable = false;
		public Boolean constant = false;
	}

	public static class Event implements Serializable {
		public String name = null;
		public List<Slot> inputs = Collections.emptyList();	 // "inputs" that receive value from Solidity contract
	}

}
