package com.unifina.signalpath.text;

import com.unifina.signalpath.*;

public class ConstantString extends AbstractConstantModule<String> {

	@Override
	protected Parameter<String> createConstantParameter() {
		return new StringParameter(this, "str", "STR");
	}

	@Override
	protected Output<String> createOutput() {
		return new StringOutput(this, "out");
	}

}
