package com.unifina.signalpath.list;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.IntegerParameter;
import com.unifina.signalpath.ListOutput;

import java.util.Collections;

public class RepeatItem extends AbstractSignalPathModule {

	private final IntegerParameter times = new IntegerParameter(this, "times", 10);
	private final Input<Object> item = new Input<>(this, "item", "Object");
	private final ListOutput listOut = new ListOutput(this, "list");

	@Override
	public void sendOutput() {
		listOut.send(Collections.nCopies(times.getValue(), item.getValue()));
	}

	@Override
	public void clearState() {}
}
