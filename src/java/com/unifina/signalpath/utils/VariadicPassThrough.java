package com.unifina.signalpath.utils;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.variadic.InputInstantiator;
import com.unifina.signalpath.variadic.OutputInstantiator;
import com.unifina.signalpath.variadic.VariadicInputOutputPair;

import java.util.List;

public class VariadicPassThrough extends AbstractSignalPathModule {

	private VariadicInputOutputPair<Object> inputOutputPairs = new VariadicInputOutputPair<>("in", "out",this,
		new InputInstantiator.SimpleObject(), new OutputInstantiator.SimpleObject(), 1);

	@Override
	public void init() {
		inputOutputPairs.init();
	}

	@Override
	public void sendOutput() {
		List<Object> values = inputOutputPairs.getInputValues();
		inputOutputPairs.sendValuesToOutputs(values);
	}

	@Override
	public void clearState() {}
}
