package com.unifina.signalpath;

import java.util.*;

/**
 * An output that may produce either a List or a Map. It uses information from a linked <code>ListOrMapInput</code> to
 * determine the appropriate output type.
 */
public class ListOrMapOutput extends Output<Object> {

	private final ListOrMapInput listOrMapInput;

	public ListOrMapOutput(AbstractSignalPathModule owner, String name, ListOrMapInput listOrMapInput) {
		super(owner, name, "Object");
		this.listOrMapInput = listOrMapInput;
	}

	@Override
	public void send(Object value) {
		List values = (List) value;
		if (listOrMapInput.receivedList()) {
			super.send(removeNulls(values));
		} else {
			super.send(formMapRemovingNulls(values, listOrMapInput.mapKeys()));
		}
	}

	private static List removeNulls(List values) {
		List withoutNulls = new ArrayList(values.size());
		for (Object value : values) {
			if (value != null) {
				withoutNulls.add(value);
			}
		}
		return withoutNulls;
	}

	private static Map<String, Object> formMapRemovingNulls(List values, List<String> keys) {
		Map<String, Object> map = new LinkedHashMap<>();
		for (int i=0; i < keys.size(); ++i) {
			Object value = values.get(i);
			if (value != null) {
				map.put(keys.get(i), value);
			}
		}
		return map;
	}
}
