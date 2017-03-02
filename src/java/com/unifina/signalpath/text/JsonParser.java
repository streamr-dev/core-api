package com.unifina.signalpath.text;

import com.unifina.signalpath.*;
import org.codehaus.groovy.grails.web.json.JSONException;
import org.codehaus.groovy.grails.web.json.JSONTokener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Parse string of JSON text into Map or List
 */
public class JsonParser extends AbstractSignalPathModule {
	private StringInput jsonInput = new StringInput(this, "json");
	private ListOutput errors = new ListOutput(this, "errors");
	private Output<Object> result = new Output<>(this, "result", "Object");

	@Override
	public void sendOutput() {
		List<String> err = new ArrayList<>();

		try {
			result.send(jsonStringToOutputObject(jsonInput.getValue()));
		} catch (JSONException e) {
			err.add(e.toString());
		}

		errors.send(err);
	}

	/**
	 * Parse a JSON string into an Object that is suitable for Output<Object>
	 * If JSON is Map or List, wrap it into UnmodifiableMap or UnmodifiableList
	 *   this is to prevent a scenario where same output goes to many modules,
	 *   and mutation of the List by one module would affect the List that the
	 *   other modules see and use
	 * @param json String that fulfills http://json.org/ specification
	 * @return Object that the JSON represents
	 * @throws JSONException if there is something wrong with the JSON string
     */
	public static Object jsonStringToOutputObject(String json) throws JSONException {
		JSONTokener parser = new JSONTokener(json);
		Object jsonObject = parser.nextValue();
		if (jsonObject instanceof Map) {
			jsonObject = Collections.unmodifiableMap((Map) jsonObject);
		} else if (jsonObject instanceof List) {
			jsonObject = Collections.unmodifiableList((List) jsonObject);
		}
		return jsonObject;
	}

	@Override
	public void clearState() { }
}
