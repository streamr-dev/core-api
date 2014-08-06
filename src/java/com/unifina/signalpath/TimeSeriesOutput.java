package com.unifina.signalpath;

import java.util.Map;

import com.unifina.utils.DU;

public class TimeSeriesOutput extends Output<Double> {

	public boolean noRepeat = true;
	public boolean canBeNoRepeat = true;
	Double previousValue = null;
	
	public TimeSeriesOutput(AbstractSignalPathModule owner, String name) {
		super(owner, name, "Double");
	}

	@Override
	public void doClear() {
		super.doClear();
		previousValue = null;
	}
	
	@Override
	public String getTypeName() {
		return "Double";
	}

	public void send(int value) {
		send(new Double(value));
	}
	
	public void send(Double value) {
		if (!noRepeat || previousValue==null || !value.equals(previousValue)) {
			value = DU.clean(value);
			
			previousValue = value;
			super.send(value);
//			for (Input<Double> target : targets) {
//				target.receive(value);
//			}
		}
	}
	
	@Override
	public Map<String,Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();
		config.put("noRepeat",noRepeat);
		config.put("canBeNoRepeat",canBeNoRepeat);
		return config;
	}

	@Override
	public void setConfiguration(Map<String,Object> config) {
		super.setConfiguration(config);
		
		if (config.containsKey("noRepeat"))
			noRepeat = Boolean.parseBoolean(config.get("noRepeat").toString());
	}
	
	@Override
	public String toString() {
		return super.toString()+": "+previousValue;
	}
	
	public Double getValue() {
		return previousValue;
	}
	
}
