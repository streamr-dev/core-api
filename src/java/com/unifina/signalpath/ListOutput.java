package com.unifina.signalpath;

import java.util.Collections;
import java.util.List;

public class ListOutput extends Output<List> {

	public ListOutput(AbstractSignalPathModule owner, String name) {
		super(owner, name, "List");
	}

	@Override
	public void send(List value) {

		// prevent modification of sent Lists (no copying, just overriding modifying methods)
		//   if a module wants to modify the list, it must make a personal copy
		super.send(Collections.unmodifiableList(value));
	}
}
