package com.unifina.signalpath.list;

import com.unifina.signalpath.*;

import java.util.List;

public class AddToList extends AbstractSignalPathModule {
	private final IntegerParameter index = new IntegerParameter(this, "index", 0);
	private final Input<Object> item = new Input<>(this, "item", "Object");
	private final ListInput listIn = new ListInput(this, "list");
	private final ListOutput listOut = new ListOutput(this, "list");
	private final StringOutput error = new StringOutput(this, "error");

	@Override
	public void sendOutput() {
		List list = listIn.getModifiableValue();
		try {
			list.add(index.getValue(), item.getValue());
			listOut.send(list);
		} catch (IndexOutOfBoundsException | IllegalArgumentException e) {
			error.send(e.getMessage());
		}
	}

	@Override
	public void clearState() {}
}
