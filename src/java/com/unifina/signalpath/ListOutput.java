package com.unifina.signalpath;

import java.util.List;

public class ListOutput extends Output<List> {

	public ListOutput(AbstractSignalPathModule owner, String name) {
		super(owner, name, "List");
	}

}
