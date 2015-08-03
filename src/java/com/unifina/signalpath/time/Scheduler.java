package com.unifina.signalpath.time;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.unifina.datasource.ITimeListener;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.TimeSeriesOutput;
import com.unifina.utils.MapTraversal;

public class Scheduler extends AbstractSignalPathModule implements ITimeListener {

	TimeSeriesOutput out = new TimeSeriesOutput(this,"value");
	
	Date nextTime;
	List<Rule> rules = new ArrayList<Rule>();
	
	@Override
	public void init() {
		addOutput(out);
	}
	
	public void clearState() {}
	
	public void sendOutput() {}
	
	@Override
	public void setTime(Date time) {
		if (nextTime==null)
			nextTime = getMinimumNextTime();
		
		// When is the next time we need to do something? Is it now?
		// if (time.equals(nextTime)) { ...
		
		// Now is the time to do something, so what do we do?
		// Get the value of the first Rule that .isActive(time) and send it to output
		
		// Once that is done, find the minimum Rule.getNext() time and set it to nextTime
	}
	
	private Date getMinimumNextTime() {
		return null;
	}
	
	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		
		// Read json rules and add Rule objects to list
		rules.clear();
		List<Map<String,Object>> ruleList = (List<Map<String,Object>>) config.get("rules");
		for (Map<String,Object> rule : ruleList) {
			rules.add(new HourlyRule(rule));
		}
	}
	
	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		
		// Read List of Rule objects and create json rules
		ArrayList<Map<String,Object>> ruleList = new ArrayList<>();
		for (Rule r : rules) {
			ruleList.add(r.getConfig());
		}
		
		config.put("rules", ruleList);
		
		return config;
	}
	
	
	abstract class Rule {
		private Map<String, Object> config;

		private Double value;

		public Rule(Map<String,Object> config) {
			this.config = config;
			value = ((Number)config.get("value")).doubleValue();
		}
		
		public abstract Date getNext(Date now);
		public abstract boolean isActive(Date now);
		
		public Double getValue() {
			return value;
		}

		public Map<String, Object> getConfig() {
			return config;
		}
	}
	
	class HourlyRule extends Rule {

		int startMinute;
		int endMinute;
		
		public HourlyRule(Map<String, Object> config) {
			super(config);
			startMinute = (int) config.get("startMinute");
			endMinute = (int) config.get("endMinute");
		}

		@Override
		public Date getNext(Date now) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(now);
			// logiikkaa
			
			return cal.getTime();
		}

		@Override
		public boolean isActive(Date now) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(now);
			// logiikkaa
			return false;
		}
		
	}

}


