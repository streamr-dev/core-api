package com.unifina.signalpath.utils;

import com.unifina.signalpath.TimeSeriesInput;

public class Filter extends PassThrough {

	private TimeSeriesInput pass = new TimeSeriesInput(this, "pass");

	@Override
	public void init() {
		addInput(pass);
		pass.setDrivingInput(false);
		pass.canToggleDrivingInput = false;
		super.init();
	}

	@Override
	public void sendOutput() {
		if (pass.getValue().equals(1D)) {
			super.sendOutput();
		}
	}
}
