package com.unifina.signalpath.utils;

import com.unifina.signalpath.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class EventTable extends ModuleWithUI {

	int eventTableInputCount = 1;
	int maxRows = 20;
	
	public EventTable() {
		super();

		// More sensible defaults, in line with default maxRows
		resendAll = false;
		resendLast = 20;
	}
	
	@Override
	public void initialize() {
		super.initialize();

		if (getGlobals().isRunContext()) {
			Map<String, Object> hdrMsg = new HashMap<String, Object>();
			hdrMsg.put("hdr", getHeaderDefinition());
			pushToUiChannel(hdrMsg);
		}
	}

	@Override
	public void sendOutput() {
		HashMap<String, Object> msg = new HashMap<String, Object>();
		ArrayList<Object> nr = new ArrayList<>(2);
		msg.put("nr", nr);
		nr.add(getGlobals().formatDateTime(getGlobals().time));

		for (Input i : getInputs()) {
			if (i.hasValue())
				nr.add(i.getValue().toString());
			else nr.add(null);
		}

		pushToUiChannel(msg);
	}

	@Override
	public void clearState() {
	}

	public Input<Object> createAndAddInput(String name) {

		Input<Object> conn = new Input<Object>(this,name,"Object");

		conn.setDrivingInput(true);
		conn.setCanToggleDrivingInput(false);
		conn.setRequiresConnection(false);
		
		// Add the input
		if (getInput(name)==null)
			addInput(conn);
			
		return conn;
	}
	
	@Override
	public boolean allInputsReady() {
		return true;
	}
	
	protected Map<String,Object> getHeaderDefinition() {
		// Table config
		Map<String,Object> headerDef = new HashMap<>();
		
		ArrayList headers = new ArrayList<>();
		headers.add("timestamp");
		for (Input<Object> i : getInputs()) {
			String name;
			if (i.isConnected())
				name = (i.getSource().getDisplayName()!=null ? i.getSource().getDisplayName() : i.getSource().getName());
			else name = i.getName();
			
			headers.add(name);
		}
		headerDef.put("headers",headers);
		return headerDef;
	}
	
	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();

		// Module options
		ModuleOptions options = ModuleOptions.get(config);
		options.add(new ModuleOption("inputs", eventTableInputCount, "int"));
		options.add(new ModuleOption("maxRows", maxRows, "int"));
		
		config.put("tableConfig", getHeaderDefinition());
		
		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		
		ModuleOptions options = ModuleOptions.get(config);
		
		if (options.getOption("inputs")!=null)
			eventTableInputCount = options.getOption("inputs").getInt();
		
		if (options.getOption("maxRows")!=null)
			maxRows = options.getOption("maxRows").getInt();
		
		for (int i = 1; i<= eventTableInputCount; i++) {
			createAndAddInput("input"+i);
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
		}
		else super.handleRequest(request, response);
	}
	
	@Override
	public void init() {

	}
	
	@Override
	public String getWebcomponentName() {
		return "streamr-table";
	}
	
}
