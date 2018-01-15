package com.unifina.signalpath.map;

import com.unifina.signalpath.*;

import java.util.*;

public class SortMap extends AbstractSignalPathModule {

	private ByParameter by = new ByParameter(this, "by", ByParameter.VALUE);
	private OrderParameter order = new OrderParameter(this, "order", OrderParameter.ASCENDING);
	private MapInput in = new MapInput(this, "in");
	private MapOutput out = new MapOutput(this, "out");

	@Override
	public void sendOutput() {
		out.send(performSort(in.getValue(), by.isByValue(), order.isDescending()));
	}

	@Override
	public void clearState() {}

	private static Map performSort(Map source, boolean sortByValue, boolean descending) {
		Map target;

		if (sortByValue) {
			if (areValuesComparable(source)) {
				target = new ValueSortedMap(descending);
			} else {
				return source; // Special case: if values are not sortable return map untouched
			}
		} else {
			target = new TreeMap<>(descending ? Collections.reverseOrder() : null);
		}

		target.putAll(source);
		return target;
	}

	private static boolean areValuesComparable(Map source) {
		return source.isEmpty() || source.values().iterator().next() instanceof Comparable;
	}

	public static class OrderParameter extends StringParameter {

		public static final String ASCENDING = "asc";
		public static final String DESCENDING = "desc";

		public OrderParameter(AbstractSignalPathModule owner, String name, String defaultValue) {
			super(owner, name, defaultValue);
		}

		@Override
		protected List<PossibleValue> getPossibleValues() {
			return Arrays.asList(
				new PossibleValue("ascending", ASCENDING),
				new PossibleValue("descending", DESCENDING)
			);
		}

		public boolean isDescending() {
			return getValue().equals(DESCENDING);
		}
	}

	public static class ByParameter extends StringParameter {

		public static final String KEY = "key";
		public static final String VALUE = "value";

		public ByParameter(AbstractSignalPathModule owner, String name, String defaultValue) {
			super(owner, name, defaultValue);
		}

		@Override
		protected List<PossibleValue> getPossibleValues() {
			return Arrays.asList(
					new PossibleValue("key", KEY),
					new PossibleValue("value", VALUE)
			);
		}

		public boolean isByValue() {
			return getValue().equals(VALUE);
		}
	}
}
