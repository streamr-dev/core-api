package com.unifina.signalpath.text;

import com.unifina.signalpath.*;
import com.unifina.utils.MapTraversal;
import org.codehaus.groovy.grails.web.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringToNumber extends AbstractSignalPathModule {
	private StringInput in = new StringInput(this, "in");
	private TimeSeriesOutput out = new TimeSeriesOutput(this, "out");
	private StringOutput error = new StringOutput(this, "error");

	@Override
	public void sendOutput() {
		try {
			out.send(Double.parseDouble(in.getValue()));
		} catch (NumberFormatException e) {
			error.send("Failed to parse: '" + in.getValue() + "'");
		}
	}

	@Override
	public void clearState() { }
}
