package com.unifina.signalpath.kafka;

import grails.converters.JSON;

import java.util.Map;

import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONObject;

import com.unifina.data.IRequireFeed;
import com.unifina.domain.data.Feed;
import com.unifina.domain.data.Stream;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.StreamParameter;
import com.unifina.signalpath.StringOutput;
import com.unifina.signalpath.TimeSeriesOutput;

public class KafkaModule extends AbstractSignalPathModule implements IRequireFeed {

	StreamParameter topic = new StreamParameter(this,"topic");
	JSONObject kafkaConfig = null;
	
	@Override
	public void init() {
		addInput(topic);
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
		
		Stream stream = topic.value;
		if (stream.getStreamConfig()==null)
			throw new IllegalStateException("Stream "+stream.getName()+" is not properly configured!");
		kafkaConfig = (JSONObject) JSON.parse(stream.getStreamConfig());

		JSONArray fields = kafkaConfig.getJSONArray("fields");
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
	}
	
	public String getTopic() {
		return kafkaConfig.get("topic").toString();
	}

	@Override
	public Feed getFeed() {
		return topic.getValue().getFeed();
	}
	
}
