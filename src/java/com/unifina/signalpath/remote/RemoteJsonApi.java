package com.unifina.signalpath.remote;

import grails.converters.JSON;

import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.web.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.StringInput;
import com.unifina.signalpath.StringParameter;
import com.unifina.signalpath.TimeSeriesOutput;

public class RemoteJsonApi extends AbstractSignalPathModule {

	StringParameter url = new StringParameter(this,"baseURL","");
	StringInput msg = new StringInput(this,"msg");
	TimeSeriesOutput score = new TimeSeriesOutput(this,"score");
	
	private static final Logger log = Logger.getLogger(RemoteJsonApi.class); 
	
	@Override
	public void init() {
		addInput(url);
		addInput(msg);
		addOutput(score);
	}

	@Override
	public void sendOutput() {
		// Do a sync request to a remote API, parsing the result as JSON
		try {
//			URIBuilder builder = new URIBuilder(url.getValue())
//			.setParameter("of", "json")
//			.setParameter("txt", msg.getValue());
			
//			String uri = builder.build().toString();
			
			HttpResponse<String> request = Unirest.get(url.getValue()+"?of=json&txt="+msg.getValue())
			  .header("X-Mashape-Authorization", "d6daq4euknsX84PFvDzwHdN3jF26qL3S")
			  .header("accept", "application/json")
			  .asString();
			
			log.info("response: "+request.getBody());
			
			JSONObject json = (JSONObject) JSON.parse(request.getBody());
			if (json.containsKey("score")) {
				score.send(Double.parseDouble(json.getString("score")));
			}
		} catch (Exception e) {
			log.error("Exception: ",e);
		}
	}

	@Override
	public void clearState() {
		
	}

}
