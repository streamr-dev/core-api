package com.unifina.signalpath.utils;

import com.unifina.signalpath.*;

public class Constant extends AbstractConstantModule<Double> {

	@Override
	protected Parameter<Double> createConstantParameter() {
		return new DoubleParameter(this, "constant", 0D);
	}

	@Override
	protected Output<Double> createOutput() {
		return new TimeSeriesOutput(this, "out");
	}

}
