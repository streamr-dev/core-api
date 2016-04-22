package com.unifina.signalpath;

import org.apache.commons.collections.list.UnmodifiableList;

import java.util.List;

public class ListOutput extends Output<List> {

	public ListOutput(AbstractSignalPathModule owner, String name) {
		super(owner, name, "List");
	}

	@Override
	public void send(List value) {

		// prevent modification of sent Lists (no copying, just overriding modifying methods)
		//   if a module wants to modify the list, it must make a personal copy
		value = UnmodifiableList.decorate(value);

		super.send(value);
	}
}
