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
	private static final java.lang.String DOUBLE_REGEX = "[-+]?([0-9]+\\.?|\\.[0-9])[0-9]*([eE][-+]?[0-9]+)?";

	private StringInput in = new StringInput(this, "in");
	private TimeSeriesOutput out = new TimeSeriesOutput(this, "out");

	private boolean strict = true;

	@Override
	public void sendOutput() {
		if (strict) {
			try {
				out.send(Double.parseDouble(in.getValue()));
			} catch (NumberFormatException e) {
				// send nothing for bad strings
			}
		} else {
			Pattern p = Pattern.compile(DOUBLE_REGEX);
			Matcher m = p.matcher(in.getValue());
			if (m.find()) {
				out.send(Double.parseDouble(m.group()));
			} else {
				out.send(0.0);
			}
		}
	}

	@Override
	public Map<String,Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		ModuleOptions options = ModuleOptions.get(config);
		options.add(new ModuleOption("strict", strict, ModuleOption.OPTION_BOOLEAN));
		return config;
	}

	@Override
	public void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		strict = MapTraversal.getBoolean(config, "options.strict.value");
	}

	@Override
	public void clearState() { }
}
