package com.unifina.signalpath.utils;

import com.unifina.signalpath.BooleanInput;

public class Filter extends PassThrough {

	private BooleanInput pass = new BooleanInput(this, "pass");

	@Override
	public void init() {
		addInput(pass);
		pass.setDrivingInput(false);
		pass.canToggleDrivingInput = false;
		super.init();
	}

	@Override
	public void sendOutput() {
		if (pass.getValue()) {
			super.sendOutput();
		}
	}
}
