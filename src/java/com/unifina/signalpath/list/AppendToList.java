package com.unifina.signalpath.list;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.ListInput;
import com.unifina.signalpath.ListOutput;

import java.util.List;

public class AppendToList extends AbstractSignalPathModule {
	private final Input<Object> item = new Input<>(this, "item", "Object");
	private final ListInput listIn = new ListInput(this, "list");
	private final ListOutput listOut = new ListOutput(this, "list");

	@Override
	public void sendOutput() {
		List list = listIn.getModifiableValue();
		list.add(item.getValue());
		listOut.send(list);
	}

	@Override
	public void clearState() {}
}
