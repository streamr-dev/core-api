package com.unifina.signalpath.random;

import com.unifina.signalpath.ListInput;
import com.unifina.signalpath.ListOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShuffleList extends ModuleWithRandomness {
	private final ListInput in = new ListInput(this, "in");
	private final ListOutput out = new ListOutput(this, "out");

	@Override
	public void init() {
		addInput(in);
		addOutput(out);
	}

	@Override
	public void sendOutput() {
		List list = new ArrayList(in.getValue());
		Collections.shuffle(list, getRandom());
		out.send(list);
	}
}
