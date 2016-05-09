package com.unifina.signalpath;

import java.util.List;
import java.util.Map;

public class BooleanInput extends Input<Boolean> {

	public BooleanInput(AbstractSignalPathModule owner, String name) {
		super(owner, name, "Object");
	}

	@Override
	public void receive(Object value) {
		if(value instanceof Boolean) {
			super.receive(value);
		} else if(value instanceof Double) {
			super.receive(!value.equals(0d) && !((Double) value).isNaN());
		} else if(value instanceof Integer) {
			super.receive(value != 0);
		} else if(value instanceof String) {
			super.receive(!value.equals("") && !value.equals("false"));
		} else if(value instanceof List) {
			super.receive(!((List) value).isEmpty());
		} else if(value instanceof Map) {
			super.receive(!((Map) value).isEmpty());
		} else
			super.receive(value != null);
	}
}
