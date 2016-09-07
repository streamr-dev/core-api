package com.unifina.signalpath.list;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.ListInput;
import com.unifina.signalpath.ListOutput;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Unique extends AbstractSignalPathModule {
	private final ListInput listIn = new ListInput(this, "list");
	private final ListOutput listOut = new ListOutput(this, "list");

	@Override
	public void sendOutput() {
		List originalList = listIn.getValue();
		Set<Object> seenItems = new HashSet<>();
		List newList = new ArrayList();
		for (Object item : originalList) {
			if (seenItems.add(item)) {
				newList.add(item);
			}
		}
		listOut.send(newList);
	}

	@Override
	public void clearState() {}
}
