package com.unifina.signalpath.utils;

import grails.converters.JSON;

import java.util.Map;

import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONObject;

import com.unifina.data.IStreamRequirement;
import com.unifina.domain.data.Stream;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.StreamParameter;
import com.unifina.signalpath.StringOutput;
import com.unifina.signalpath.TimeSeriesOutput;

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
public class ConfigurableStreamModule extends AbstractSignalPathModule implements IStreamRequirement {

	protected StreamParameter streamParameter = new StreamParameter(this,"stream");
	protected JSONObject streamConfig = null;
	
	@Override
	public void init() {
		addInput(streamParameter);
		streamParameter.setCheckModuleId(true);
	}

	@Override
	public void sendOutput() {

	}

	@Override
	public void clearState() {

	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		
		Stream stream = streamParameter.value;
		if (stream.getStreamConfig()==null)
			throw new IllegalStateException("Stream "+stream.getName()+" is not properly configured!");
		streamConfig = (JSONObject) JSON.parse(stream.getStreamConfig());

		JSONArray fields = streamConfig.getJSONArray("fields");
		for (Object o : fields) {
			JSONObject j = (JSONObject) o;
			String type = j.getString("type");
			String name = j.getString("name");
			
			// TODO: add other types
			if (type.equalsIgnoreCase("double")) {
				addOutput(new TimeSeriesOutput(this,name));
			}
			else if (type.equalsIgnoreCase("string")) {
				addOutput(new StringOutput(this,name));
			}
			else if (type.equalsIgnoreCase("boolean")) {
				
			}
		}
		
		if (streamConfig.containsKey("name"))
			this.setName(streamConfig.get("name").toString());
	}
	
	@Override
	public Stream getStream() {
		return streamParameter.getValue();
	}
	
}
