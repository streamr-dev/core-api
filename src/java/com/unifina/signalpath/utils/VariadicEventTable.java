package com.unifina.signalpath.utils;

import com.unifina.push.PushChannel;
import com.unifina.signalpath.*;
import com.unifina.signalpath.variadic.InputInstantiator;
import com.unifina.signalpath.variadic.VariadicInput;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class VariadicEventTable extends ModuleWithUI {

	private VariadicInput<Object> ins = new VariadicInput<>(this, new EventTableInputInstantiator());

	int maxRows = 20;
	private boolean showOnlyNewValues = false;

	@Override
	public void init() {
	}

	public VariadicEventTable() {
		super();

		// More sensible defaults, in line with default maxRows
		resendAll = false;
		resendLast = 20;
	}

	@Override
	public void initialize() {
		super.initialize();

		PushChannel rc = null;

		if (getGlobals().getUiChannel() != null) {
			rc = getGlobals().getUiChannel();
		}

		if (rc != null) {
			Map<String, Object> hdrMsg = new HashMap<String, Object>();
			hdrMsg.put("hdr", getHeaderDefinition());
			getGlobals().getUiChannel().push(hdrMsg, uiChannelId);
		}
	}

	@Override
	public void sendOutput() {
		PushChannel rc = getGlobals().getUiChannel();
		if (rc != null) {
			HashMap<String, Object> msg = new HashMap<String, Object>();
			ArrayList<Object> nr = new ArrayList<>(2);
			msg.put("nr", nr);
			nr.add(getGlobals().dateTimeFormat.format(getGlobals().time));

			for (Input<Object> i : ins.getEndpoints()) {
				if (i.hasValue() && (!showOnlyNewValues || drivingInputs.contains(i))) {
					nr.add(i.getValue().toString());
				} else {
					nr.add(null);
				}
			}

			rc.push(msg, uiChannelId);
		}
	}

	@Override
	public void clearState() {
	}

	@Override
	public boolean allInputsReady() {
		return true;
	}

	@Override
	public Input getInput(String name) {
		Input input = super.getInput(name);
		if (input == null) {
			input = ins.addEndpoint(name);
		}
		return input;
	}

	private Map<String, Object> getHeaderDefinition() {
		// Table config
		Map<String, Object> headerDef = new HashMap<>();

		ArrayList headers = new ArrayList<>();
		headers.add("timestamp");
		for (Input<Object> i : ins.getEndpoints()) {
			String name;
			if (i.isConnected()) {
				name = i.getSource().getEffectiveName();
			} else {
				name = i.getEffectiveName();
			}

			headers.add(name);
		}
		headerDef.put("title", getUiChannelName());
		headerDef.put("headers", headers);
		return headerDef;
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		ModuleOptions options = ModuleOptions.get(config);
		options.add(ModuleOption.createInt("maxRows", maxRows));
		options.add(ModuleOption.createBoolean("showOnlyNewValues", showOnlyNewValues));
		config.put("tableConfig", getHeaderDefinition());

		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		ins.onConfiguration(config);

		ModuleOptions options = ModuleOptions.get(config);
		if (options.getOption("maxRows") != null) {
			maxRows = options.getOption("maxRows").getInt();
		}
		if (options.getOption("showOnlyNewValues") != null) {
			showOnlyNewValues = options.getOption("showOnlyNewValues").getBoolean();
		}
	}

	@Override
	protected void handleRequest(RuntimeRequest request, RuntimeResponse response) {
		if (request.getType().equals("initRequest")) {
			// We need to support unauthenticated initRequests for public views, so no authentication check

			Map<String, Object> hdrMsg = new HashMap<String, Object>();
			hdrMsg.put("hdr", getHeaderDefinition());
			response.put("initRequest", hdrMsg);
			response.setSuccess(true);
		} else {
			super.handleRequest(request, response);
		}
	}

	@Override
	public String getWebcomponentName() {
		return "streamr-table";
	}

	private static class EventTableInputInstantiator implements InputInstantiator<Object>, Serializable {
		@Override
		public Input<Object> instantiate(AbstractSignalPathModule module, String endpointName) {
			Input<Object> input = new Input<>(module, endpointName, "Object");
			input.setDrivingInput(true);
			input.canToggleDrivingInput = false;
			input.canBeFeedback = false;
			input.requiresConnection = false;
			return input;
		}
	}

}
