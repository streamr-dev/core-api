package com.unifina.signalpath.utils;

import com.unifina.data.IStreamRequirement;
import com.unifina.domain.data.Stream;
import com.unifina.signalpath.*;
import grails.converters.JSON;
import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This module creates inputs and outputs on configuration time
 * with regard to a streamConfig given in the database.
 * 
 * The streamConfig must be a JSON message with a key "fields": a list of
 * (name,type) pairs.
 * 
 * The sreamConfig can also define a key "name", which is used as the module name.
 * 
 * This module works well with the MapMessageEventRecipient event recipient class.
 * @author Henri
 */
public class ConfigurableStreamModule extends AbstractStreamSourceModule implements IStreamRequirement {

	transient protected JSONObject streamConfig = null;

	@Override
	public void sendOutput() {

	}

	@Override
	public void clearState() {

	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		
		Stream stream = getStream();
		if (stream.getConfig() == null) {
			throw new IllegalStateException("Stream "+stream.getName()+" is not properly configured!");
		}
		streamConfig = (JSONObject)JSON.parse(stream.getConfig());

		JSONArray fields = streamConfig.getJSONArray("fields");
		
		for (Object o : fields) {
			JSONObject j = (JSONObject)o;
			String type = j.getString("type");
			String name = j.getString("name");

			Output output = null;

			if (type.equalsIgnoreCase("number")) {
				output = new TimeSeriesOutput(this, name);
			} else if (type.equalsIgnoreCase("string")) {
				output = new StringOutput(this, name);
			} else if (type.equalsIgnoreCase("boolean")) {
				output = new BooleanOutput(this, name);
			} else if (type.equalsIgnoreCase("map")) {
				output = new MapOutput(this, name);
			} else if (type.equalsIgnoreCase("list")) {
				output = new ListOutput(this, name);
			}

			if (output != null) {
				if (output instanceof PrimitiveOutput) {
					((PrimitiveOutput) output).setNoRepeat(false);
				}
				addOutput(output);
			}
		}
		
		if (streamConfig.containsKey("name"))
			this.setName(streamConfig.get("name").toString());
	}

}
