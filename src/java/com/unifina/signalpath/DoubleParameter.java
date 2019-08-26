package com.unifina.signalpath;


public class DoubleParameter extends Parameter<Double> {

	public DoubleParameter(AbstractSignalPathModule owner, String name, Double defaultValue) {
		super(owner, name, defaultValue, "Double");
	}
	
	@Override
	public Double parseValue(String s) {
		Double result;
		try {
			result = Double.parseDouble(s);
		} catch (NumberFormatException e) {
			String msg = String.format("Module %s's parameter '%s' cannot parse value '%s'", getOwner().getDisplayName(), getName(), s);
			throw new RuntimeException(msg, e);
		}
		return result;
	}
	
}
