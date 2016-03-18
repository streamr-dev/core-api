package com.unifina.signalpath;

public class TimeSeriesInput extends PrimitiveInput<Double> {

	public TimeSeriesInput(AbstractSignalPathModule owner, String name) {
		super(owner, name, "Double");
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
