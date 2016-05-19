package com.unifina.signalpath.utils;

import com.unifina.push.PushChannel;
import com.unifina.signalpath.*;
import com.unifina.signalpath.variadic.InputInstantiator;
import com.unifina.signalpath.variadic.VariadicInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class EventTable extends ModuleWithUI {

	private VariadicInput<Object> inputs = new VariadicInput<>(this, new InputInstantiator<Object>() {

		@Override
		public Input<Object> instantiate(AbstractSignalPathModule module, String endpointName) {
			Input<Object> input = new Input<>(module, endpointName, "Object");
			input.setDrivingInput(true);
			input.canToggleDrivingInput = false;
			input.canBeFeedback = false;
			input.requiresConnection = false;
			return input;
		}
	});

	int maxRows = 20;

	@Override
	public void init() {}
	
	public EventTable() {
		super();
		canClearState = false;
		
		// More sensible defaults, in line with default maxRows
		resendAll = false;
		resendLast = 20;
	}
	
	@Override
	public void initialize() {
		super.initialize();

		PushChannel rc = null;

		if (globals.getUiChannel()!=null && !globals.getSignalPathContext().containsKey("csv")) {
			rc = globals.getUiChannel();
		}
		
		if (rc!=null) {
			Map<String,Object> hdrMsg = new HashMap<String,Object>();
			hdrMsg.put("hdr", getHeaderDefinition());
			globals.getUiChannel().push(hdrMsg, uiChannelId);
		}
	}

	@Override
	public void sendOutput() {
		PushChannel rc = globals.getUiChannel();
		if (rc != null) {
			HashMap<String, Object> msg = new HashMap<String, Object>();
			ArrayList<Object> nr = new ArrayList<>(2);
			msg.put("nr", nr);
			nr.add(globals.dateTimeFormat.format(globals.time));

			for (Input<Object> i : inputs.getEndpoints()) {
				if (i.hasValue()) {
					nr.add(i.getValue().toString());
				} else {
					nr.add(null);
				}
			}

			rc.push(msg, uiChannelId);
		}
	}

	@Override
	public void clearState() {}
	
	@Override
	public boolean allInputsReady() {
		return true;
	}

	@Override
	public Input getInput(String name) {
		Input input = super.getInput(name);
		if (input == null) {
			input = inputs.addEndpoint(name);
		}
		return input;
	}

	private Map<String,Object> getHeaderDefinition() {
		// Table config
		Map<String,Object> headerDef = new HashMap<>();
		
		ArrayList headers = new ArrayList<>();
		headers.add("timestamp");
		for (Input<Object> i : inputs.getEndpoints()) {
			String name;
			if (i.isConnected()) {
				name = (i.getSource().getDisplayName() != null ? i.getSource().getDisplayName() : i.getSource().getName());
			} else {
				name = i.getName();
			}
			
			headers.add(name);
		}
		headerDef.put("headers", headers);
		return headerDef;
	}
	
	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();

		ModuleOptions options = ModuleOptions.get(config);
		options.add(new ModuleOption("maxRows", maxRows, "int"));
		config.put("tableConfig", getHeaderDefinition());

		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		inputs.onConfiguration(config);
		
		ModuleOptions options = ModuleOptions.get(config);
		if (options.getOption("maxRows") != null) {
			maxRows = options.getOption("maxRows").getInt();
		}
	}
	
	@Override
	protected void handleRequest(RuntimeRequest request, RuntimeResponse response) {
		if (request.getType().equals("initRequest")) {
			// We need to support unauthenticated initRequests for public views, so no authentication check
			
			Map<String,Object> hdrMsg = new HashMap<String,Object>();
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
	
}
