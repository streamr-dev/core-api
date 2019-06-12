package com.unifina.feed;

import com.streamr.client.protocol.message_layer.StreamMessage;
import com.unifina.domain.data.Stream;
import com.unifina.utils.MapTraversal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Automatically detects fields of given Stream by analyzing recent data.
 */
public abstract class FieldDetector {
	private boolean flattenMap = false;

	public void setFlattenMap(boolean flattenMap) {
		this.flattenMap = flattenMap;
	}

	/**
	 * Returns a list of fields in a MapMessage by fetching an example message from the Stream.
	 * May return null if the example message couldn't be fetched (Stream is empty for example).
     */
	public List<Map<String, String>> detectFields(Stream stream) {
		StreamMessage msg = fetchExampleMessage(stream);
		if (msg == null) {
			return null;
		}

		Map map;
		try {
			map = msg.getContent();
		} catch (IOException e) {
			map = new HashMap();
		}

		if (flattenMap) {
			map = MapTraversal.flatten(map);
		}

		List<Map<String, String>> fields = new ArrayList<>();
		for (Object key : map.keySet()) {
			Map<String, String> field = new HashMap<>();
			field.put("name", key.toString());
			field.put("type", detectType(map.get(key)));
			fields.add(field);
		}
		return fields;
	}

	private String detectType(Object o) {
		if (o instanceof Boolean) {
			return "boolean";
		} else if (o instanceof Number) {
			return "number";
		} else if (o instanceof List) {
			return "list";
		} else if (o instanceof Map) {
			return "map";
		} else {
			return "string";
		}
	}

	protected abstract StreamMessage fetchExampleMessage(Stream stream);
}
