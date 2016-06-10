package com.unifina.signalpath.map;

import com.unifina.signalpath.*;
import org.apache.log4j.Logger;

import java.util.*;

public class SortMap extends AbstractSignalPathModule {
	private static final Logger log = Logger.getLogger(SortMap.class);

	private BooleanParameter byValue = new BooleanParameter(this, "byValue", false);
	private OrderingParameter isDescending = new OrderingParameter(this, "ordering", false);
	private MapInput in = new MapInput(this, "in");
	private MapOutput out = new MapOutput(this, "out");

	@Override
	public void sendOutput() {
		Map source = in.getValue();
		Map target = byValue.getValue() ?
			new ValueSortedMap(isDescending.getValue()) :
			new TreeMap<>(isDescending.getValue() ? Collections.reverseOrder() : null);

		try {
			target.putAll(source);
		} catch (ClassCastException e) {
			return;
		}

		if (target != null) {
			out.send(target);
		}
	}

	@Override
	public void clearState() {}

	public static class OrderingParameter extends BooleanParameter {
		public OrderingParameter(AbstractSignalPathModule owner, String name, boolean defaultIsDescending) {
			super(owner, name, defaultIsDescending);
		}
		@Override
		protected List<PossibleValue> getPossibleValues() {
			return Arrays.asList(
				new PossibleValue("ascending", "asc"),		// false
				new PossibleValue("descending", "desc")		// true
			);
		}
		@Override
		public Boolean parseValue(String s) {
			return "desc".equals(s);
		}
	}
}
