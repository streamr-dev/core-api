package com.unifina.signalpath.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.IReturnChannel;
import com.unifina.signalpath.Input;


public class EventTable extends AbstractSignalPathModule {
	
	private IReturnChannel rc;
	private SimpleDateFormat df;
	
	public EventTable() {
		super();
		canClearState = false;
	}
	
	@Override
	public void initialize() {
		super.initialize();

		if (parentSignalPath!=null && parentSignalPath.getReturnChannel()!=null && !globals.getSignalPathContext().containsKey("csv")) {
			rc = parentSignalPath.getReturnChannel();
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
			for (Input i : getInputs())
				nr.add(i.getValue().toString());
			rc.sendPayload(hash,msg);
		}
	}

	@Override
	public void clearState() {
	}

	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();
		
		Map<String,Object> tableConfig = new HashMap<>();
		config.put("tableConfig",tableConfig);
		
		ArrayList headers = new ArrayList<>();
		headers.add("timestamp");
		headers.add("event"); // TODO: generalize for many inputs
		tableConfig.put("headers",headers);
		tableConfig.put("rows",Integer.MAX_VALUE);
		
		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		
		// TODO: generalize for many inputs
		addInput(new Input<Object>(this,"event","Object"));
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}
	
}
