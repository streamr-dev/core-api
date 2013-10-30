package com.unifina.signalpath.charts;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.unifina.signalpath.Input;
import com.unifina.signalpath.RecordedTimeSeriesInput;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.utils.MapTraversal;
import com.unifina.utils.TimeOfDayUtil;

public class TimeSeriesChart extends Chart {
	
	int inputCount = 10;
	
	TimeSeriesInput[] myInputs;
	
	boolean overnightBreak = true;
	
	@Override
	public void initialize() {
		super.initialize();
		
		if (!csv) {
			ArrayList<String> names = new ArrayList<>();
			int seriesIdx = 0;
			
			ArrayList<Series> seriesData = new ArrayList<>();
			
			for (Input input : getInputs()) {
				RecordedTimeSeriesInput it = (RecordedTimeSeriesInput) input;
				
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

					seriesData.add(new Series(it.seriesName,it.seriesIndex,true,it.yAxis));
				}
			}

			if (hasRc) {
				parentSignalPath.returnChannel.sendPayload(hash, new InitMessage(seriesData, null));
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
	
	@Override
	public void addDefaultInputs() {
		// see onConfiguration()
//		addInput(inputNumber)
	}
	
	public TimeSeriesInput getInputConnection(String name) {

		TimeSeriesInput conn;
		if (csv)
			conn = new TimeSeriesInput(this,name);
		else {
			conn = new RecordedTimeSeriesInput(this,name);
			((RecordedTimeSeriesInput)conn).yAxis = getInputs().length;
		}

		conn.canBeDrivingInput = true;
		conn.canHaveInitialValue = false;
		conn.canBeFeedback = false;
		conn.suppressWarnings = true;
		
		// Add the input
		if (getInput(name)==null)
			addInput(conn);
			
		return conn;
	}
	
	@Override
	protected void record() {
//		if (todUtil!=null && !todUtil.hasBaseDate())
//			todUtil.setBaseDate(globals.time);
		
//		if (todUtil==null || todUtil.isInRange(globals.time)) {
			for (Input i : drivingInputs) {
				RecordedTimeSeriesInput input = (RecordedTimeSeriesInput) i;
				if (!Double.isNaN(input.value) && hasRc) {
					PointMessage msg = new PointMessage(
							input.seriesIndex, 
							globals.getTzConverter().getFakeLocalTime(globals.time.getTime()),
							input.value);
					//[type:"p", s:input.seriesIndex, x:globals.tzConverter.getFakeLocalTime(globals.time.getTime()), y:input.value]
					parentSignalPath.returnChannel.sendPayload(hash, msg);
				}
			}
//		}
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
		
//		if (todUtil!=null)
//			todUtil.clearBaseDate();
		
		if (!csv && hasRc && overnightBreak) {
			for (Input it : getInputs()) {
				if (it instanceof RecordedTimeSeriesInput && it.isConnected()) {
					// Send day break
					parentSignalPath.returnChannel.sendPayload(hash, new BreakMessage(((RecordedTimeSeriesInput)it).seriesIndex));
				}
			}
		}
	}
	
	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();
		Map optionsMap = (Map) config.get("options");

		LinkedHashMap<String, Object> inputMap = new LinkedHashMap<>();
		optionsMap.put("inputs",inputMap);
		
		inputMap.put("value",inputCount);
		inputMap.put("type","int");
		
		LinkedHashMap<String, Object> overnightBreakOption = new LinkedHashMap<>();
		optionsMap.put("overnightBreak",overnightBreakOption);
		
		overnightBreakOption.put("value",overnightBreak);
		overnightBreakOption.put("type","boolean");
		
		return config;
	}
	
	@Override
	public void onConfiguration(Map config) {
		super.onConfiguration(config);
		
		Map options = (Map)config.get("options");
		
		// New
		if (options!=null) {
			// Add a DoubleParameter for each AR param
			inputCount = (int) MapTraversal.getProperty(options, "inputs.value");
			
			if (MapTraversal.getProperty(options, "overnightBreak.value")!=null) {
				overnightBreak = Boolean.parseBoolean(MapTraversal.getProperty(options, "overnightBreak.value").toString());
			}
		}
		// Backwards compatibility
		else if (config.containsKey("params")) {
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
	
}
