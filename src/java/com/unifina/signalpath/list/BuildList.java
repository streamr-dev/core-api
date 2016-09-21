package com.unifina.signalpath.list;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.ListOutput;
import com.unifina.signalpath.variadic.InputInstantiator;
import com.unifina.signalpath.variadic.VariadicInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BuildList extends AbstractSignalPathModule {
	private final VariadicInput<Object> ins = new VariadicInput<>(this, new InputInstantiator.SimpleObject(), 0);
	private final ListOutput listOut = new ListOutput(this, "out");

	@Override
	public void sendOutput() {
		List list = new ArrayList<>();
		for (Input<Object> in : ins.getEndpoints()) {
			list.add(in.getValue());
		}
		listOut.send(list);
	}

	@Override
	public void clearState() {}

	@Override
	public Input getInput(String name) {
		Input input = super.getInput(name);
		if (input == null) {
			input = ins.addEndpoint(name);
		}
		return input;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		ins.onConfiguration(config);
	}
}
