package com.unifina.feed;

import com.unifina.domain.data.Stream;
import com.unifina.feed.map.MapMessage;
import org.codehaus.groovy.grails.commons.GrailsApplication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Automatically detects fields of given Stream by analyzing recent data.
 */
public abstract class FieldDetector {
	protected final GrailsApplication grailsApplication;
	private boolean flattenMap;

	public FieldDetector(GrailsApplication grailsApplication) {
		this.grailsApplication = grailsApplication;
	}

	public Map<String, String> detectFields(Stream stream) {
		MapMessage map = fetchExampleMessage(stream);
		Map<String, String> fields = new HashMap<>();

		for (Object key : map.payload.keySet()) {
			fields.put(key.toString(), detectType(map.payload.get(key)));
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
