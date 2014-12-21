package com.unifina.signalpath.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.unifina.push.PushChannel;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.ModuleOption;
import com.unifina.signalpath.ModuleOptions;
import com.unifina.signalpath.ModuleWithUI;


public class EventTable extends ModuleWithUI {
	
	private PushChannel rc;
	private SimpleDateFormat df;
	
	int inputCount = 1;
	int maxRows = 0;
	
	public EventTable() {
		super();
		canClearState = false;
	}
	
	@Override
	public void initialize() {
		super.initialize();

		if (globals.getUiChannel()!=null && !globals.getSignalPathContext().containsKey("csv")) {
			rc = globals.getUiChannel();
		}
		
		if (rc!=null) {
			Map<String,Object> hdrMsg = new HashMap<String,Object>();
			hdrMsg.put("hdr", getHeaderDefinition());
			globals.getUiChannel().push(hdrMsg, uiChannelId);
		}
		
		df = globals.dateTimeFormat;
	}

	@Override
	public void sendOutput() {
		if (rc!=null) {
			HashMap<String,Object> msg = new HashMap<String,Object>();
			ArrayList<Object> nr = new ArrayList<>(2);
			msg.put("nr", nr);
			nr.add(df.format(globals.time));
			
			for (Input i : getInputs()) {
				if (i.hasValue())
					nr.add(i.getValue().toString());
				else nr.add(null);
			}
			
			rc.push(msg, uiChannelId);
		}
	}

	@Override
	public void clearState() {
	}

	public Input<Object> createAndAddInput(String name) {

		Input<Object> conn = new Input<Object>(this,name,"Object");

		conn.setDrivingInput(true);
		conn.canToggleDrivingInput = false;
		conn.canBeFeedback = false;
		conn.requiresConnection = false;
		
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
		options.add(new ModuleOption("inputs", inputCount, "int"));
		options.add(new ModuleOption("maxRows", maxRows, "int"));
		
		config.put("tableConfig", getHeaderDefinition());
		
		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		
		ModuleOptions options = ModuleOptions.get(config);
		
		if (options.getOption("inputs")!=null)
			inputCount = options.getOption("inputs").getInt();
		
		if (options.getOption("maxRows")!=null)
			maxRows = options.getOption("maxRows").getInt();
		
		for (int i=1;i<=inputCount;i++) {
			createAndAddInput("input"+i);
		}
	}
	
	@Override
	public void init() {

	}
	
}
