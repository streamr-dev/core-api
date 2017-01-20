package com.unifina.signalpath;

import java.util.ArrayList;
import java.util.List;

public class ListInput extends Input<List> {

	public ListInput(AbstractSignalPathModule owner, String name) {
		super(owner, name, "List");
	}

	/**
	 * @return (shallow) copy of the input List that can be freely modified
	 */
	public List getModifiableValue() {
		return new ArrayList(getValue());
	}

}
