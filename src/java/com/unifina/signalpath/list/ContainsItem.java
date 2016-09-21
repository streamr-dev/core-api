package com.unifina.signalpath.list;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.BooleanOutput;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.ListInput;

public class ContainsItem extends AbstractSignalPathModule {
	private final ListInput list = new ListInput(this, "list");
	private final Input<Object> item = new Input<>(this, "item", "Object");
	private final BooleanOutput found = new BooleanOutput(this, "found");

	@Override
	public void sendOutput() {
		found.send(list.getValue().contains(item.getValue()));
	}

	@Override
	public void clearState() {}
}
