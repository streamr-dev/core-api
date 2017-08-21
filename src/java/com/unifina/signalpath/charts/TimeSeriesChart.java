package com.unifina.signalpath.charts;

import com.unifina.signalpath.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TimeSeriesChart extends Chart {
	
	private int tsInputCount = 10;
	private boolean barify = false;
	
	@Override
	public void initialize() {
		super.initialize();

		ArrayList<String> names = new ArrayList<>();
		int seriesIdx = 0;

		for (Input input : getInputs()) {
			TimeSeriesChartInput it = (TimeSeriesChartInput) input;

			it.setInitialValue(Double.NaN);

			// Set names and series indices
			if (it.isConnected()) {
				it.seriesIndex = seriesIdx++;

				String newName = (it.getSource().getDisplayName() != null ? it.getSource().getDisplayName() : it.getSource().getOwner().getName()+"."+it.getSource().getName());

				// Watch out for overlapping names
				int i = 1;
				String origName = newName;
				while (names.contains(newName)) {
					i++;
					newName = origName+i;
				}
				names.add(newName);
				it.seriesName = newName;
			}
		}
		if (getGlobals().isRunContext()) {
			pushToUiChannel(getInitMessage());
		}
	}
	
	protected InitMessage getInitMessage() {
		ArrayList<Series> seriesData = new ArrayList<>();
		
		for (Input input : getInputs()) {
			TimeSeriesChartInput it = (TimeSeriesChartInput) input;
			
			// Set names and series indices
			if (it.isConnected()) {
				seriesData.add(new Series(it.seriesName,it.seriesIndex,true,it.yAxis));
			}
		}

		return new InitMessage(getUiChannelName(), seriesData, null);
	}
	
	public TimeSeriesInput getInputConnection(String name) {

		TimeSeriesChartInput conn = new TimeSeriesChartInput(this,name);
		// Assign to yAxis 0 by default
		conn.yAxis = 0;

		conn.setDrivingInput(true);
		conn.setCanToggleDrivingInput(false);
		conn.setCanHaveInitialValue(false);
		conn.setRequiresConnection(false);
		
		// Add the input
		if (getInput(name) == null) {
			addInput(conn);
		}
			
		return conn;
	}
	
	@Override
	protected void record() {
		for (Input i : getDrivingInputs()) {
			TimeSeriesChartInput input = (TimeSeriesChartInput) i;
			if (!Double.isNaN(input.value)
					&& (!barify || getGlobals().time.getTime() - input.previousTime >= 60000L)) {
				
					PointMessage msg = new PointMessage(
							input.seriesIndex, 
							getGlobals().getTzConverter().getFakeLocalTime(getGlobals().time.getTime()),
							input.value);
					
					pushToUiChannel(msg);
					input.previousTime = getGlobals().time.getTime();
			}
		}
	}

	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();

		config.put("barify",barify);
		
		ModuleOptions options = ModuleOptions.get(config);
		options.add(new ModuleOption("inputs", tsInputCount, ModuleOption.OPTION_INTEGER));
		
		return config;
	}
	
	@Override
	public void onConfiguration(Map config) {
		super.onConfiguration(config);
		
		if (config.containsKey("barify"))
			barify = Boolean.parseBoolean(config.get("barify").toString());
		
		ModuleOptions options = ModuleOptions.get(config);
		
		if (options.getOption("inputs")!=null)
			tsInputCount = options.getOption("inputs").getInt();
		
		// Backwards compatibility
		if (config.containsKey("params")) {
			List params = (List) config.get("params");
			Map inputConfig = null;
			for (Object p : params) {
				if (((Map)p).get("name").equals("inputs")) {
					inputConfig = (Map)p;
				}
			}
			if (inputConfig != null)
				tsInputCount = Integer.parseInt(inputConfig.get("value").toString());
		}
		
		for (int i=1;i<=tsInputCount;i++) {
			getInputConnection("in"+i);
		}
	}
	
	@Override
	protected void handleRequest(RuntimeRequest request, RuntimeResponse response) {
		if (request.getType().equals("initRequest")) {
			// We need to support unauthenticated initRequests for public views, so no authentication check
			
			response.put("initRequest", getInitMessage());
			response.setSuccess(true);
		} else {
			super.handleRequest(request, response);
		}
	}
}
