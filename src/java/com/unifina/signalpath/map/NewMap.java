package com.unifina.signalpath.map;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.BooleanParameter;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.MapOutput;

import java.util.LinkedHashMap;
import java.util.Map;

public class NewMap extends AbstractSignalPathModule {
	private BooleanParameter alwaysNew = new BooleanParameter(this, "alwaysNew", false);
	private Input<Object> trigger = new Input<>(this, "trigger", "Object");
	private MapOutput out = new MapOutput(this, "out");

	private Map map = new LinkedHashMap();

	public NewMap() {
		trigger.setDrivingInput(true);
	}

	@Override
	public void sendOutput() {
		if (alwaysNew.getValue()) {
			map = new LinkedHashMap<>();
		}
		out.send(map);
	}

	@Override
	public void clearState() {}
}
