package com.unifina.signalpath;

import java.util.List;
import java.util.Map;

public class BooleanInput extends PrimitiveInput<Boolean> {

	public BooleanInput(AbstractSignalPathModule owner, String name) {
		super(owner, name, "Object");
	}

	@Override
	public void receive(Object value) {
		if(value instanceof Boolean) {
			super.receive(value);
		} else if(value instanceof Number) {
			super.receive(((Number)value).doubleValue() != 0d);
		} else if(value instanceof String) {
			super.receive(!value.equals("") && !value.equals("false"));
		} else if(value instanceof List) {
			super.receive(!((List) value).isEmpty());
		} else if(value instanceof Map) {
			super.receive(!((Map) value).isEmpty());
		} else
			super.receive(value != null);
	}

	@Override
	protected Boolean parseInitialValue(String initialValue) {
		return Boolean.parseBoolean(initialValue);
	}
}
