package com.unifina.signalpath.list;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.ListInput;
import com.unifina.signalpath.ListOutput;

import java.util.ArrayList;
import java.util.List;

public class IndexesOfItem extends AbstractSignalPathModule {
	private final ListInput list = new ListInput(this, "list");
	private final Input<Object> item = new Input<>(this, "item", "Object");
	private final ListOutput indexes = new ListOutput(this, "indexes");

	@Override
	public void sendOutput() {
		List<Double> matches = new ArrayList<>();
		for (int i=0; i < list.getValue().size(); ++i) {
			if (list.getValue().get(i).equals(item.getValue())) {
				matches.add((double)i);
			}
		}
		indexes.send(matches);
	}

	@Override
	public void clearState() {}
}
