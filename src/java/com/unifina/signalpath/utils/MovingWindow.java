package com.unifina.signalpath.utils;

import com.unifina.signalpath.AbstractModuleWithWindow;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.ListOutput;
import com.unifina.utils.window.TimedValue;
import com.unifina.utils.window.WindowListener;

import java.util.ArrayList;
import java.util.List;

public class MovingWindow extends AbstractModuleWithWindow<Object> {
	private final Input<Object> in = new Input<>(this, "in", "Object");
	private final ListOutput list = new ListOutput(this, "list");

	@Override
	protected void handleInputValues() {
		addToWindow(in.getValue());
	}

	@Override
	protected void doSendOutput() {
		List<Object> lst = new ArrayList<>();
		for (Object o : get1DWindow()) {
			if (o instanceof TimedValue) {
				lst.add(((TimedValue) o).value);
			} else {
				lst.add(o);
			}
		}
		list.send(lst);
	}

	@Override
	protected WindowListener<Object> createWindowListener(Object key) {
		return null;
	}
}
