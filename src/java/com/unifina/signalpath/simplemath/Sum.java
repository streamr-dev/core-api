package com.unifina.signalpath.simplemath;

import java.io.Serializable;
import java.util.LinkedList;

import com.unifina.signalpath.*;
import com.unifina.utils.window.WindowListener;

public class Sum extends AbstractModuleWithWindow<Double> {

	IntegerParameter minSamples = new IntegerParameter(this,"minSamples",1);
	TimeSeriesInput input = new TimeSeriesInput(this,"in");
	TimeSeriesOutput out = new TimeSeriesOutput(this,"out");

	Double sum = 0D;
	int count = 0;

	@Override
	protected void handleInputValues() {
		addToWindow(input.getValue());
		count++;
	}

	@Override
	protected void doSendOutput() {
		if (count>=minSamples.getValue())
			out.send(sum);
	}

	@Override
	public void clearState() {
		super.clearState();
		count = 0;
	}


	@Override
	protected WindowListener<Double> createWindowListener(int dimension) {
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
