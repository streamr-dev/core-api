package com.unifina.signalpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An input that accepts both Maps and Lists. Will always return an <code>Iterable</code> to module, but will keep
 * special information stored for <code>ListOrMapOutput</code> later on for creating the appropriate output.
 */
public class ListOrMapInput extends Input<Iterable> {

	private List<String> keys;

	public ListOrMapInput(AbstractSignalPathModule owner, String name) {
		super(owner, name, "List Map");
	}

	@Override
	public void receive(Object value) {
		if (value instanceof Map) {
			keys = new ArrayList<>();
			List<Object> values = new ArrayList<>();
			for (Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
				keys.add(entry.getKey());
				values.add(entry.getValue());
			}
			super.receive(values);
		} else {
			keys = null;
			super.receive(value);
		}
	}

	boolean receivedList() {
		return keys == null;
	}

	List<String> mapKeys() {
		return keys == null ? null : Collections.unmodifiableList(keys);
	}
}
