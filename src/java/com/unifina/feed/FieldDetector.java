package com.unifina.feed;

import com.unifina.data.FeedEvent;
import com.unifina.data.IEventRecipient;
import com.unifina.domain.data.Stream;
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

	public FieldDetector(GrailsApplication grailsApplication) {
		this.grailsApplication = grailsApplication;
	}

	/**
	 * Returns a list of fields in a MapMessage by fetching an example message from the Stream.
	 * May return null if the example message couldn't be fetched (Stream is empty for example).
     */
	public List<Map<String, String>> detectFields(Stream stream) {
		AbstractStreamrMessage msg = fetchExampleMessage(stream);
		if (msg == null) {
			return null;
		}

		// Msg may wrap many events, so get the latest one
		FeedEvent<AbstractStreamrMessage, IEventRecipient>[] events = msg.toFeedEvents(null);
		if (events.length == 0) {
			return null;
		}
		msg = events[events.length - 1].content;

		List<Map<String, String>> fields = new ArrayList<>();
		for (String key : msg.keySet()) {
			Map<String, String> field = new HashMap<>();
			field.put("name", key);
			field.put("type", detectType(msg.get(key)));
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

	protected abstract AbstractStreamrMessage fetchExampleMessage(Stream stream);
}
