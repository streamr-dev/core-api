package com.unifina.signalpath;


public class IntegerParameter extends Parameter<Integer> {

	public IntegerParameter(AbstractSignalPathModule owner, String name,
			Integer defaultValue) {
		super(owner, name, defaultValue, "Double");
	}

	@Override
	public void receive(Object value) {
		if (value instanceof Double)
			super.receive(((Double)value).intValue());
		else super.receive(value);
	}
	
	@Override
	public Integer parseValue(String s) {
		return (int) Double.parseDouble(s);
	}
	
	@Override
	protected Integer handlePulledObject(Object o) {
		if (o instanceof Double)
			return ((Double)o).intValue();
		else return super.handlePulledObject(o);
	}
	
}
