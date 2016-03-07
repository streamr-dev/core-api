package com.unifina.signalpath.map;

public class CountByKey extends AggregateByKey {
	@Override
	protected double onSendOutput(String key, Double currentValue) {
		return currentValue == null ? 1 : currentValue + 1;
	}

	@Override
	protected void onKeyDropped(String key, Double lastValue) {}
}
