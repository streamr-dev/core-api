package com.unifina.signalpath;


public class DoubleParameter extends Parameter<Double> {

	public DoubleParameter(AbstractSignalPathModule owner, String name, Double defaultValue) {
		super(owner, name, defaultValue, "Double");
	}
	
	@Override
	public Double parseValue(String s) {
		return Double.parseDouble(s);
	}
	
}
