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

	StringParameter method = new StringParameter(this,"method","POST");
	StringParameter url = new StringParameter(this,"baseURL","");
	StringParameter lang = new StringParameter(this,"lang","en");
	StringParameter exclude = new StringParameter(this,"exclude","#awayd");
	
	StringInput text = new StringInput(this,"text");
	
	TimeSeriesOutput value = new TimeSeriesOutput(this,"value");
	
	private static final Logger log = Logger.getLogger(RemoteJsonApi.class); 
	
	@Override
	public void init() {
		addInput(method);
		addInput(url);
		
		addInput(lang);
		addInput(exclude);
		
		addInput(text);
		
		addOutput(value);
	}

	@Override
	public void sendOutput() {
		// Do a sync request to a remote API, parsing the result as JSON
		try {
			HttpResponse<String> request = Unirest.post(url.getValue())
			  .header("X-Mashape-Authorization", "FMKib7yel0BtlhoLNgvKlygcD5c5div3")
			  .header("accept", "application/json")
			  .field("lang", lang.value)
			  .field("exclude", exclude.value)
			  .field("text", text.value)
			  .asString();
			
			log.info("response: "+request.getBody());
			
			JSONObject json = (JSONObject) JSON.parse(request.getBody());
			if (json.containsKey("value")) {
				value.send(Double.parseDouble(json.getString("value")));
			}
			else value.send(0);
		} catch (Exception e) {
			log.error("Exception: ",e);
			value.send(0);
		}
	}

	@Override
	public void clearState() {
		
	}

}
