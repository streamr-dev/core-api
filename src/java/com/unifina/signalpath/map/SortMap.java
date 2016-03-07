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
			target = copyIntoValueSortedMap(source);
		}

		if (target != null) {
			out.send(target);
		}
	}

	private LinkedHashMap copyIntoValueSortedMap(Map source) {
		try {
			List<Map.Entry> list = new LinkedList(source.entrySet());
			Collections.sort(list, new Comparator<Map.Entry>() {
				@Override
				public int compare(Map.Entry o1, Map.Entry o2) {
					return ((Comparable) o1.getValue()).compareTo(o2.getValue());
				}
			});

			LinkedHashMap target = new LinkedHashMap();
			for (Map.Entry entry : list) {
				target.put(entry.getKey(), entry.getValue());
			}
			return target;
		} catch (ClassCastException e) {
			log.debug("Could not sort by value because value was not Comparable.", e);
			return null;
		}
	}

	@Override
	public void clearState() {

	}
}
