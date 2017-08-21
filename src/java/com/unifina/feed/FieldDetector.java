package com.unifina.feed;

import com.unifina.domain.data.Stream;
import com.unifina.feed.map.MapMessage;
import com.unifina.utils.MapTraversal;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Automatically detects fields of given Stream by analyzing recent data.
 */
public abstract class FieldDetector {
	protected final GrailsApplication grailsApplication;
	private boolean flattenMap = false;

	public FieldDetector(GrailsApplication grailsApplication) {
		this.grailsApplication = grailsApplication;
	}

	public void setFlattenMap(boolean flattenMap) {
		this.flattenMap = flattenMap;
	}

	/**
	 * Returns a list of fields in a MapMessage by fetching an example message from the Stream.
	 * May return null if the example message couldn't be fetched (Stream is empty for example).
     */
	public List<Map<String, String>> detectFields(Stream stream) {
		MapMessage mapMessage = fetchExampleMessage(stream);
		if (mapMessage == null) {
			return null;
		}

		Map map = mapMessage.payload;

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

	protected abstract MapMessage fetchExampleMessage(Stream stream);
}
