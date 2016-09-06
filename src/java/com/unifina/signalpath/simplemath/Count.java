package com.unifina.signalpath.simplemath;

import com.unifina.signalpath.AbstractModuleWithWindow;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.TimeSeriesOutput;
import com.unifina.utils.window.WindowListener;

public class Count extends AbstractModuleWithWindow<Object> {

	private Input<Object> input = new Input<>(this, "in", "Object");
	private TimeSeriesOutput out = new TimeSeriesOutput(this, "count");
	
	private double count = 0;

	@Override
	protected void handleInputValues() {
		addToWindow(input.getValue());
	}

	@Override
	protected void doSendOutput() {
		out.send(count);
	}

	@Override
	protected WindowListener<Object> createWindowListener(Object key) {
		return new WindowListener<Object>() {

			@Override
			public void onAdd(Object item) {
				++count;
			}

			@Override
			public void onRemove(Object item) {
				--count;
			}

			@Override
			public void onClear() {
				count = 0;
			}
		};
	}
}
