package com.unifina.signalpath.simplemath;

import java.io.Serializable;
import java.util.LinkedList;

import com.unifina.signalpath.*;
import com.unifina.utils.window.WindowListener;

public class Sum extends AbstractModuleWithWindow<Double> {

	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	TimeSeriesOutput out = new TimeSeriesOutput(this,"out");

	Double sum = 0D;

	@Override
	protected void handleInputValues() {
		addToWindow(input.getValue());
	}

	@Override
	protected void doSendOutput() {
		out.send(sum);
	}

	@Override
	public void clearState() {
		super.clearState();
	}

	@Override
	protected WindowListener<Double> createWindowListener(Object key) {
		return new SumWindowListener();
	}

	class SumWindowListener implements WindowListener<Double>, Serializable {
		@Override
		public void onAdd(Double item) {
			sum += item;
		}

		@Override
		public void onRemove(Double item) {
			sum -= item;
		}

		@Override
		public void onClear() {
			sum = 0D;
		}
	}
}
