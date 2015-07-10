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
	TimeSeriesOutput matchAny = new TimeSeriesOutput(this,"matchAny?");
	TimeSeriesOutput count = new TimeSeriesOutput(this,"matchCount");
	ListOutput list = new ListOutput(this,"matchList");
	
	Pattern p = null;
	Matcher m = null;
		
	@Override
	public void init() {
		addInput(s);
		addInput(in);
		addOutput(match);
		addOutput(matchAny);
		addOutput(count);
		addOutput(list);
	}

	@Override
	public void sendOutput() {
		String text = in.getValue();
		if(!s.getValue().isEmpty()){
			if(p == null || !p.toString().equals(s.getValue()))
				p = Pattern.compile(s.getValue());
			m = p.matcher(text);
			
			// Matches
			if(match.isConnected()){
				if(m.matches())
					match.send(1);
				else
					match.send(0);
			}
			if(matchAny.isConnected()){
				if(m.find())
					matchAny.send(1);
				else
					matchAny.send(0);
			}
			if(count.isConnected() || list.isConnected()){
				int i = 0;
				ArrayList<String> l = new ArrayList<String>();
				while(m.find()){
					i++;
					if(list.isConnected()){
						l.add(m.group());
					}
				}
				if(count.isConnected())
					count.send(i);
				if(list.isConnected())
					list.send(l);
			}
		}
		
	}

	@Override
	public void clearState() {
		p = null;
		m = null;
	}
	
}
