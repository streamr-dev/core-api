package com.unifina.signalpath.map;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.BooleanParameter;
import com.unifina.signalpath.MapInput;
import com.unifina.signalpath.MapOutput;
import org.apache.log4j.Logger;

import java.util.*;

public class SortMap extends AbstractSignalPathModule {
	private static final Logger log = Logger.getLogger(SortMap.class);

	private BooleanParameter byValue = new BooleanParameter(this, "byValue", false);
	private MapInput in = new MapInput(this, "in");
	private MapOutput out = new MapOutput(this, "out");

	@Override
	public void sendOutput() {
		Map source = in.getValue();
		Map target;

		if (!byValue.getValue()) {
			target = new TreeMap<>(source);
		} else {
			target = new ValueSortedMap(true);
			try {
				target.putAll(source);
			} catch (ClassCastException e) {
				return;
			}
		}

		if (target != null) {
			out.send(target);
		}
	}

	@Override
	public void clearState() {}
}
