package com.unifina.signalpath.time;

import com.unifina.datasource.ITimeListener;
import com.unifina.signalpath.ModuleWithUI;
import com.unifina.signalpath.TimeSeriesOutput;

import java.util.*;

public class Scheduler extends ModuleWithUI implements ITimeListener {

	private final TimeSeriesOutput out = new TimeSeriesOutput(this, "value");

	private final List<Rule> rules = new ArrayList<>();
	private double defaultValue = 0;
	private transient Date nextActivationTime;

	@Override
	public void init() {
		resendLast = 1;
		addOutput(out);
	}

	public void sendOutput() {}

	public void clearState() {
		nextActivationTime = null;
	}

	@Override
	public void setTime(Date time) {
		if (nextActivationTime == null) {
			nextActivationTime = getMinimumNextTime(new Date(time.getTime() - 1000));
		}

		if (out.getValue() == null || time.equals(nextActivationTime)) {
			List<Integer> activeRules = new ArrayList<>();
			for (int i=0; i < rules.size(); ++i) {
				if (rules.get(i).isActive(time)) {
					activeRules.add(i);
				}
			}

			if (activeRules.isEmpty()) {
				out.send(defaultValue);
			} else {
				Rule firstActiveRule = rules.get(activeRules.get(0));
				out.send(firstActiveRule.getValue());
			}

			pushToUiChannel(Collections.singletonMap("activeRules", activeRules));
			nextActivationTime = null;
		}
	}

	private Date getMinimumNextTime(Date time) {
		Date firstNextTime = null;
		for (Rule r : rules) {
			Date next = r.getNext(time);
			if (firstNextTime == null || next.before(firstNextTime)) {
				firstNextTime = next;
			}
		}
		return firstNextTime;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		Map<String, Object> scheduleConfig = (Map<String, Object>) config.get("schedule");

		if (scheduleConfig != null) {
			List<Map<String, Object>> ruleConfigList = (List<Map<String, Object>>) scheduleConfig.get("rules");
			defaultValue = ((Number) scheduleConfig.get("defaultValue")).doubleValue();

			rules.clear();
			for (Map<String, Object> ruleConfig : ruleConfigList) {
				rules.add(Rule.instantiateRule(ruleConfig, getGlobals().getUserTimeZone()));
			}
		}
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		List<Map<String, Object>> ruleConfigList = new ArrayList<>();
		for (Rule r : rules) {
			ruleConfigList.add(r.getConfig());
		}

		Map<String, Object> scheduleConfig = new HashMap<>();
		scheduleConfig.put("rules", ruleConfigList);
		scheduleConfig.put("defaultValue", defaultValue);
		config.put("schedule", scheduleConfig);

		return config;
	}
}
