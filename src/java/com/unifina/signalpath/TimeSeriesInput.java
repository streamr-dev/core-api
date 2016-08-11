package com.unifina.signalpath;

public class TimeSeriesInput extends PrimitiveInput<Double> {

	public TimeSeriesInput(AbstractSignalPathModule owner, String name) {
		super(owner, name, "Double");
	}

	@Override
	public void receive(Object value) {
		super.receive(((Number) value).doubleValue()); // Ensure that integers are received as Doubles
	}

	@Override
	protected Double parseInitialValue(String initialValue) {
		return Double.parseDouble(initialValue);
	}

	@Override
	protected boolean validateInitialValue(Double initialValue) {
		return !initialValue.isNaN() && !initialValue.isInfinite();
	}
}
