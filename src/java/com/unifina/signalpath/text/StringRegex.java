package com.unifina.signalpath.text;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.ListOutput;
import com.unifina.signalpath.StringInput;
import com.unifina.signalpath.StringParameter;
import com.unifina.signalpath.TimeSeriesOutput;
import com.unifina.utils.MapTraversal;


public class StringRegex extends AbstractSignalPathModule {
	
	StringParameter s = new StringParameter(this, "pattern", "");
	
	StringInput in = new StringInput(this,"text");

	TimeSeriesOutput match = new TimeSeriesOutput(this,"match?");
	TimeSeriesOutput count = new TimeSeriesOutput(this,"matchCount");
	ListOutput list = new ListOutput(this,"matchList");
	
	Pattern p = null;
	transient Matcher m = null;

	boolean lastIgnoreCase = false;
	boolean ignoreCase = false;
		
	@Override
	public void init() {
		addInput(s);
		addInput(in);
		addOutput(match);
		addOutput(count);
		addOutput(list);
	}

	@Override
	public void sendOutput() {
		String text = in.getValue();
		int matchCount = 0;
		ArrayList<String> matchList = new ArrayList<String>();
		if(!s.getValue().isEmpty()){
			String pattern = s.getValue();

			// Setup or reset matcher
			if (p == null || lastIgnoreCase != ignoreCase || !p.toString().equals(pattern)) {
				lastIgnoreCase = ignoreCase;
				if (ignoreCase) {
					p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
				} else {
					p = Pattern.compile(pattern);
				}
				m = p.matcher(text);
			} else {
				if (m == null) {  // m may be null after deserialization
					m = p.matcher(text);
				}
				m.reset(text);
			}

			// Find and store matches
			while (m.find()) {
				matchCount++;
				matchList.add(m.group());

				if (!count.isConnected() && !list.isConnected()) {
					break;
				}
			}

			// Send results to outputs
			if (match.isConnected()) {
				match.send(matchCount > 0 ? 1 : 0);
			}
			if (count.isConnected()) {
				count.send(matchCount);
			}
			if (list.isConnected()) {
				list.send(matchList);
			}
		}
	}

	@Override
	public void clearState() {
		p = null;
		m = null;
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String,Object> config = super.getConfiguration();
		
		if (!config.containsKey("options")) {
			config.put("options", new HashMap<String,Object>());
		}
		Map<String,Object> options = (Map<String,Object>) config.get("options");
		
		Map<String,Object> caseOption = new HashMap<>();
		options.put("ignoreCase", caseOption);
		caseOption.put("type","boolean");
		caseOption.put("value",ignoreCase);
		
		return config;
	}
	
	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		if (MapTraversal.getProperty(config, "options.ignoreCase.value")!=null) {
			ignoreCase = MapTraversal.getBoolean(config, "options.ignoreCase.value"); 
		}
	}
	
}
