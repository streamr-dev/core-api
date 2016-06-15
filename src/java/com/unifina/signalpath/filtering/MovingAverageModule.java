package com.unifina.signalpath.filtering;

import com.unifina.signalpath.AbstractModuleWithWindow;
import com.unifina.signalpath.TimeSeriesInput;
import com.unifina.signalpath.TimeSeriesOutput;
import com.unifina.utils.window.WindowListener;

public class MovingAverageModule extends AbstractModuleWithWindow<Double> {

	private TimeSeriesInput input = new TimeSeriesInput(this, "in");
	private TimeSeriesOutput out = new TimeSeriesOutput(this, "out");

	private double sum = 0D;
	private long count = 0;


	@Override
	protected void handleInputValues() {
		addToWindow(input.getValue());
	}

	@Override
	protected void doSendOutput() {
		out.send(sum / count);
	}

	@Override
	protected WindowListener<Double> createWindowListener(Object key) {
		return new WindowListener<Double>() {
			@Override
			public void onAdd(Double item) {
				sum += item;
				++count;
			}

			@Override
			public void onRemove(Double item) {
				sum -= item;
				--count;
			}

			@Override
			public void onClear() {
				sum = 0D;
				count = 0;
			}
		};
	}
}
