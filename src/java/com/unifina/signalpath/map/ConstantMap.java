package com.unifina.signalpath.map;

import com.unifina.signalpath.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class ConstantMap extends AbstractConstantModule<Map> {

	@Override
	protected Parameter<Map> createConstantParameter() {
		return new MapParameter(this, "map", new LinkedHashMap<String, Object>());
	}

	@Override
	protected Output<Map> createOutput() {
		return new MapOutput(this,"out");
	}
}
