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
		Map source = in.getValue();
		Map target = by.isByValue() ?
			new ValueSortedMap(order.isDescending()) :
			new TreeMap<>(order.isDescending() ? Collections.reverseOrder() : null);

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
