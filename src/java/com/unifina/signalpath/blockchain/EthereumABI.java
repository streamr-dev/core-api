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

public class EthereumABI {

	private static final Logger log = Logger.getLogger(EthereumABI.class);

	private Function constructor = null;
	private List<Function> functions = new ArrayList<>();

	private List<Event> events = new ArrayList<>();
	private final String json;

	public EthereumABI(List<Map<String, Object>> abiAsMap) {
		this(new Gson().toJsonTree(abiAsMap).getAsJsonArray());
	}

	public EthereumABI(String abiAsJson) {
		this(new JsonParser().parse(abiAsJson).getAsJsonArray());
	}

	public EthereumABI(JsonArray jsonArray) {
		Gson gson = new Gson();
		this.json = gson.toJson(jsonArray);

		log.info("Parsing interface: " + jsonArray);

		for (JsonElement element : jsonArray) {
			JsonObject functionOrEvent = element.getAsJsonObject();
			String type = functionOrEvent.get("type").getAsString();
			if (type == null) {
				type = "function";
			}

			if (type.equals("function") || type.equals("fallback")) {
				EthereumABI.Function f = gson.fromJson(functionOrEvent, EthereumABI.Function.class);
				log.info("Found function " + f.name + (f.constant ? " [constant]" : "") + (f.payable ? " [payable]" : ""));
				functions.add(f);
			} else if (type.equals("constructor")) {
				EthereumABI.Function f = gson.fromJson(functionOrEvent, EthereumABI.Function.class);
				log.info("Found constructor " + f.name + (f.constant ? " [constant]" : "") + (f.payable ? " [payable]" : ""));
				constructor = f;
			} else if (functionOrEvent.get("type").getAsString().equals("event")) {
				EthereumABI.Event e = gson.fromJson(functionOrEvent, EthereumABI.Event.class);
				log.info("Found event " + e.name);
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
		public String name = "(default)";
		public List<Slot> inputs = Collections.emptyList();
		public List<Slot> outputs = Collections.emptyList();
		public Boolean payable = false;
		public Boolean constant = false;
	}

	public static class Event implements Serializable {
		public String name = null;
		public List<Slot> inputs = Collections.emptyList();				// "inputs" that receive value from Solidity contract
		public List<Output<Object>> outputs;	// "outputs" from Streamr module on canvas
	}

}
