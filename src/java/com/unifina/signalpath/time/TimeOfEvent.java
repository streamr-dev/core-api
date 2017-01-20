package com.unifina.signalpath.time;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.TimeSeriesOutput;

public class TimeOfEvent extends AbstractSignalPathModule {
	private final Input<Object> trigger = new Input<>(this, "trigger", "Object");
	private final TimeSeriesOutput timestamp = new TimeSeriesOutput(this, "timestamp");

	@Override
	public void init() {
		trigger.canToggleDrivingInput = false;
		addInput(trigger);
		addOutput(timestamp);
	}

	@Override
	public void sendOutput() {
		timestamp.send(getGlobals().time.getTime());
	}

	@Override
	public void clearState() {}
}
