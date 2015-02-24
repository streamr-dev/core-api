package com.unifina.signalpath.charts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.unifina.signalpath.Input;
import com.unifina.signalpath.ModuleOption;
import com.unifina.signalpath.ModuleOptions;
import com.unifina.signalpath.TimeSeriesChartInput;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.utils.MapTraversal;

public class TimeSeriesChart extends Chart {
	
	int inputCount = 10;
	boolean barify = true;
	
	TimeSeriesInput[] myInputs;
	
	Boolean overnightBreak = true;
	
	@Override
	public void initialize() {
		super.initialize();
		
		if (!csv) {
			ArrayList<String> names = new ArrayList<>();
			int seriesIdx = 0;
			
			ArrayList<Series> seriesData = new ArrayList<>();
			
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

			if (hasRc) {
				globals.getUiChannel().push(getInitMessage(), uiChannelId);
			}
		}
		else {

			// Write names of input sources as header titles into the outfile
			ArrayList<String> names = new ArrayList<String>();
			ArrayList<TimeSeriesInput> connectedInputs = new ArrayList<>();

			csvWriter.writeField("timestamp");

			for (Input inp : getInputs()) {
				TimeSeriesInput input = (TimeSeriesInput)inp;
				if (input.isConnected()) {
					String name = (input.source.getDisplayName()!=null ? input.source.getDisplayName() : input.source.owner.getName()+"."+input.source.getName());

					// Watch out for overlapping names
					int i = 1;
					String origName = name;
					while (names.contains(name)) {
						i++;
						name = origName+i;
					}

					names.add(name);
					csvWriter.writeField(name);
					connectedInputs.add(input);
				}
				else {
					input.setInitialValue(0.0);
				}
			}
			csvWriter.newLine();
			myInputs = connectedInputs.toArray(new TimeSeriesInput[connectedInputs.size()]);

			if (MapTraversal.getProperty(globals.getSignalPathContext(), "csvOptions.filterEmpty")!=null 
					&& (boolean)MapTraversal.getProperty(globals.getSignalPathContext(), "csvOptions.filterEmpty")==false) {
				for (TimeSeriesInput it : myInputs)
					it.setInitialValue(Double.NaN);
			}
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

		return new InitMessage(seriesData, null);
	}
	
	public TimeSeriesInput getInputConnection(String name) {

		TimeSeriesInput conn;
		if (csv)
			conn = new TimeSeriesInput(this,name);
		else {
			conn = new TimeSeriesChartInput(this,name);
			// Assign to yAxis 0 by default
			((TimeSeriesChartInput)conn).yAxis = 0;
		}

		conn.setDrivingInput(true);
		conn.canToggleDrivingInput = false;
		conn.canHaveInitialValue = false;
		conn.canBeFeedback = false;
		conn.requiresConnection = false;
		
		// Add the input
		if (getInput(name)==null)
			addInput(conn);
			
		return conn;
	}
	
	@Override
	protected void record() {
		for (Input i : drivingInputs) {
			TimeSeriesChartInput input = (TimeSeriesChartInput) i;
			
			if (!Double.isNaN(input.value) 
					&& hasRc 
					&& (!barify || globals.time.getTime() - input.previousTime >= 60000L)) {
				
					PointMessage msg = new PointMessage(
							input.seriesIndex, 
							globals.getTzConverter().getFakeLocalTime(globals.time.getTime()),
							input.value);
					
					globals.getUiChannel().push(msg, uiChannelId);
					
					input.previousTime = globals.time.getTime();
			}
		}
	}

	@Override
	protected void recordCsvString() {
		csvWriter.writeField(globals.time);

		for (int i=0;i<myInputs.length;i++) {
			Double v = myInputs[i].getValue();
			if (v!=null && !v.equals(Double.NaN))
				csvWriter.writeField(Double.toString(myInputs[i].value));
			else csvWriter.writeField("");
		}
	}
	
	@Override
	public void clearState() {
		super.clearState();
		
		if (!csv && hasRc && overnightBreak) {
			for (Input it : getInputs()) {
				if (it instanceof TimeSeriesChartInput && it.isConnected()) {
					// Send day break
					globals.getUiChannel().push(new BreakMessage(((TimeSeriesChartInput)it).seriesIndex), uiChannelId);
				}
			}
		}
	}
	
	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();

		config.put("barify",barify);
		
		ModuleOptions options = ModuleOptions.get(config);
		options.add(new ModuleOption("inputs", inputCount, "int"));
		options.add(new ModuleOption("overnightBreak", overnightBreak, "boolean"));
		
		return config;
	}
	
	@Override
	public void onConfiguration(Map config) {
		super.onConfiguration(config);
		
		if (config.containsKey("barify"))
			barify = Boolean.parseBoolean(config.get("barify").toString());
		
		ModuleOptions options = ModuleOptions.get(config);
		
		if (options.getOption("inputs")!=null)
			inputCount = options.getOption("inputs").getInt();
		if (options.getOption("overnightBreak")!=null)
			overnightBreak = options.getOption("overnightBreak").getBoolean();
		
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
				inputCount = Integer.parseInt(inputConfig.get("value").toString());
		}
		
		for (int i=1;i<=inputCount;i++) {
			getInputConnection("in"+i);
		}
	}

	@Override
	protected void addDefaultInputs() {
		// inputs added in onConfiguration()
	}
	
	@Override
	public Future<Map<String, Object>> receiveUIMessage(Map message) {
		if (message.get("type").equals("initRequest")) {
			/**
			 * The TimeSeriesChart be queried for InitMessage
			 */
			final Map<String,Object> response = new HashMap<>();
			FutureTask<Map<String,Object>> future = new FutureTask<>(new Runnable() {
				@Override
				public void run() {
					response.put("initRequest", getInitMessage());
				}
			}, response);
			message.put("future", future);
			super.receiveUIMessage(message);
			return future;
		}
		else return super.receiveUIMessage(message);
	}
	
	@Override
	protected void handleUIMessage(Map message) {
		if (message.get("type").equals("initRequest")) {
			FutureTask<Map<String,Object>> future = (FutureTask<Map<String,Object>>) message.get("future");
			future.run();
		}
		else super.handleUIMessage(message);
	}
	
}
