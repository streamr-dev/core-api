package com.unifina.signalpath;

import java.util.*;

public class BooleanParameter extends Parameter<Boolean> {
	
	public BooleanParameter(AbstractSignalPathModule owner, String name, Boolean defaultValue) {
		super(owner, name, defaultValue, "Boolean");
	}

	@Override
	protected List<PossibleValue> getPossibleValues() {
		return Arrays.asList(
			new PossibleValue("false", "false"),
			new PossibleValue("true", "true")
		);
	}

	@Override
	public Boolean parseValue(String s) {
		return Boolean.parseBoolean(s);
	}
}
