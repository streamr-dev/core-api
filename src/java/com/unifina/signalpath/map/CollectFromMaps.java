package com.unifina.signalpath.map;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.ListOrMapInput;
import com.unifina.signalpath.ListOrMapOutput;
import com.unifina.signalpath.StringParameter;
import com.unifina.utils.MapTraversal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CollectFromMaps extends AbstractSignalPathModule {

	private final StringParameter selector = new StringParameter(this, "selector", "");
	private final ListOrMapInput input = new ListOrMapInput(this, "listOrMap");
	private final ListOrMapOutput output = new ListOrMapOutput(this, "listOrMap", input);

	@Override
	public void sendOutput() {
		Iterable<Map> maps = input.getValue();
		if (selector.getValue().isEmpty()) {
			output.send(maps);
		} else {
			List<Object> results = new ArrayList<>();
			for (Map map : maps) {
				Object value = MapTraversal.getProperty(map, selector.getValue());
				results.add(value);
			}
			output.send(results);
		}
	}

	@Override
	public void clearState() {

	}
}
