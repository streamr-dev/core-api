package com.unifina.signalpath.text;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.ListOutput;
import com.unifina.signalpath.StringInput;
import com.unifina.signalpath.StringParameter;
import com.unifina.signalpath.TimeSeriesOutput;

public class StringRegex extends AbstractSignalPathModule {
	
	StringParameter s = new StringParameter(this, "pattern", "");
	
	StringInput in = new StringInput(this,"text");

	TimeSeriesOutput match = new TimeSeriesOutput(this,"match?");
	TimeSeriesOutput count = new TimeSeriesOutput(this,"matchCount");
	ListOutput list = new ListOutput(this,"matchList");
	
	Pattern p = null;
	Matcher m = null;
		
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
			if(p == null || !p.toString().equals(s.getValue())){
				p = Pattern.compile(s.getValue());
				m = p.matcher(text);
			} else
				m.reset(text);
			
			while (m.find()) {
				matchCount++;
				matchList.add(m.group());
				if(!count.isConnected() && !list.isConnected()) {
					break;
				}
			}

			if(match.isConnected()) {
				if(matchCount > 0)
					match.send(1);
				else
					match.send(0);
			}
			if(count.isConnected())
				count.send(matchCount);
			if(list.isConnected())
				list.send(matchList);
		}
	}

	@Override
	public void clearState() {
		p = null;
		m = null;
	}
	
}
