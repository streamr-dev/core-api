package com.unifina.signalpath.bool;

import com.unifina.signalpath.*;

public class BooleanConstant extends AbstractConstantModule<Boolean> {

	@Override
	protected Parameter<Boolean> createConstantParameter() {
		return new BooleanParameter(this, "val", true);
	}

	@Override
	protected Output<Boolean> createOutput() {
		return new BooleanOutput(this, "out");
	}
}
