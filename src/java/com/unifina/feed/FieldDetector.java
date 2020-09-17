package com.unifina.feed;

import com.streamr.client.protocol.message_layer.StreamMessage;
import com.unifina.utils.MapTraversal;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Automatically detects fields of given Stream by analyzing recent data.
 */
public class FieldDetector {

	private static final Logger log = Logger.getLogger(FieldDetector.class);

	enum FieldType {
		Boolean("boolean"),
		Number("number"),
		List("list"),
		Map("map"),
		String("string");

		private String asString;

		FieldType(String asString) {
			this.asString = asString;
		}
	}

	/**
	 * Returns a list of fields in a StreamMessage based on its content.
	 * Returns null if the given msg is null. If flatten is true, this will
	 * flatten nested Map hierarchies to flat maps with dot-separated field names.
     */
	public static List<FieldConfig> detectFields(StreamMessage msg, boolean flatten) {
		if (msg == null) {
			return null;
		}

		Map<String, Object> map = msg.getParsedContent();

		if (flatten) {
			map = MapTraversal.flatten(map);
		}

		List<FieldConfig> fields = new ArrayList<>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			fields.add(new FieldConfig(entry.getKey(), detectType(entry.getValue())));
		}
		return fields;
	}

	private static FieldType detectType(Object o) {
		if (o instanceof Boolean) {
			return FieldType.Boolean;
		} else if (o instanceof Number) {
			return FieldType.Number;
		} else if (o instanceof List) {
			return FieldType.List;
		} else if (o instanceof Map) {
			return FieldType.Map;
		} else if (o instanceof String) {
			return FieldType.String;
		} else {
			log.warn("Could not detect type of value: " + o + "class: " + (o == null ? "null" : o.getClass()));
			return FieldType.String; // fallback
		}
	}

	static class FieldConfig {

		private String name;
		private FieldType type;

		public FieldConfig(String name, FieldType type) {
			this.name = name;
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public FieldType getType() {
			return type;
		}

		public Map<String, String> toMap() {
			Map<String, String> field = new HashMap<>();
			field.put("name", name);
			field.put("type", type.asString);
			return field;
		}

	}

}
