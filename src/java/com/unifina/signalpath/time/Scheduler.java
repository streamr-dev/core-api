package com.unifina.signalpath.time;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.unifina.datasource.ITimeListener;
import com.unifina.signalpath.AbstractSignalPathModule;
import com.unifina.signalpath.TimeSeriesOutput;

public class Scheduler extends AbstractSignalPathModule implements ITimeListener {

	TimeSeriesOutput out = new TimeSeriesOutput(this, "value");

	Date nextTime;
	List<Rule> rules = new ArrayList<Rule>();
	Double defaultValue;

	@Override
	public void init() {
		addOutput(out);
	}

	public void clearState() {
	}

	public void sendOutput() {
	}

	@Override
	public void setTime(Date time) {
		if (nextTime == null)
			nextTime = getMinimumNextTime();

		if (time.equals(nextTime)) {
			for(Rule r : rules){
				if(r.isActive(time)){
					out.send(r.getValue());
					nextTime = getMinimumNextTime();
					break;
				}
			}
		}
	}

	private Date getMinimumNextTime() {
		Date firstNextTime = null;
		for(Rule r : rules){
			Date next = r.getNext(globals.time);
			if(firstNextTime == null || next.before(firstNextTime)){
				firstNextTime = nextTime;
			}
		}
		return firstNextTime;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);

		// Read json rules and add Rule objects to list
		rules.clear();
		List<Map<String, Object>> ruleList = (List<Map<String, Object>>) config.get("rules");
		defaultValue = ((Number)config.get("defaultValue")).doubleValue();
		if (ruleList != null) {
			for (Map<String, Object> rule : ruleList) {
				int type = (int) rule.get("intervalType");
				switch (type) {
				case 0:
					rules.add(new HourlyRule(rule));
				case 1:
					rules.add(new DailyRule(rule));
				case 2:
					rules.add(new WeeklyRule(rule));
				case 3:
					rules.add(new MonthlyRule(rule));
				case 4:
					rules.add(new YearlyRule(rule));
				}

			}
		}
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		// Read List of Rule objects and create json rules
		ArrayList<Map<String, Object>> ruleList = new ArrayList<>();
		for (Rule r : rules) {
			ruleList.add(r.getConfig());
		}

		config.put("rules", ruleList);
		config.put("defaultValue", defaultValue);

		return config;
	}
	
	public List<Rule> getRules(){
		return rules;
	}

	abstract class Rule {
		private Map<String, Object> config;

		private Double value;

		private List<Integer> dateFields = Arrays.asList(new Integer[] {
				Calendar.MONTH, Calendar.DATE, Calendar.HOUR, Calendar.MINUTE,
				Calendar.SECOND, Calendar.MILLISECOND });
		private List<Integer> dayOfWeekFields = Arrays.asList(new Integer[] {
				Calendar.MONTH, Calendar.WEEK_OF_YEAR, Calendar.DAY_OF_WEEK,
				Calendar.HOUR, Calendar.MINUTE, Calendar.SECOND,
				Calendar.MILLISECOND });
		
		private Calendar cal = Calendar.getInstance();

		public HashMap<Integer, Integer> startTargets = new HashMap<>();
		public HashMap<Integer, Integer> endTargets = new HashMap<>();
		
		public Rule(Map<String, Object> config) {
			this.config = config;
			value = ((Number)config.get("value")).doubleValue();
		}

		public Date getNext(Date now) {
			if (isActive(now))
				return getNext(now, endTargets);
			else
				return getNext(now, startTargets);
		}

		public boolean isActive(Date now) {
			Date nextStart = getNext(now, startTargets);
			Date nextEnd = getNext(now, endTargets);
			return nextEnd.before(nextStart);
		}

		public Double getValue() {
			return value;
		}

		public Map<String, Object> getConfig() {
			return config;
		}

		private Date getNext(Date now, Map<Integer, Integer> targets) {
			cal.setTime(now);

			Date date = now;
			int target;
			List<Integer> fields = null;
			Integer[] targetFields = Arrays.copyOf(targets.keySet().toArray(),
					targets.keySet().toArray().length, Integer[].class);

			for (int i = 0; i < targets.size(); i++) {
				int field = targetFields[i];
				target = targets.get(field);
				if (field == Calendar.HOUR)
					field = Calendar.HOUR_OF_DAY;

				int valueNow = cal.get(field);

				if (fields == null) {
					if (field == Calendar.DAY_OF_WEEK) {
						fields = dayOfWeekFields;
						valueNow++;
						if(valueNow == 7)
							valueNow = 0;
					} else {
						fields = dateFields;
					}
				}

				if (valueNow != target) {
					if (valueNow > target) {
						int fieldToRaise = fields.get(fields
								.indexOf(targetFields[0]) - 1);
						cal.add(fieldToRaise, 1);
					}
					for (int j = fields.indexOf(field); j < fields.size(); j++) {
						if (targets.containsKey(fields.get(j))) {
							cal.set(fields.get(j), targets.get(fields.get(j)));
						} else {
							cal.set(fields.get(j), 0);
						}
					}
				}
			}
			return cal.getTime();
		}
	}

	class HourlyRule extends Rule {
		int startMinute;
		int endMinute;

		public HourlyRule(Map<String, Object> config) {
			super(config);

			startMinute = (int) ((Map) config.get("startDate")).get("minute");

			endMinute = (int) ((Map) config.get("endDate")).get("minute");

			startTargets.put(Calendar.MINUTE, startMinute);

			endTargets.put(Calendar.MINUTE, endMinute);
		}
	}

	class DailyRule extends Rule {
		int startHour;
		int startMinute;

		int endHour;
		int endMinute;

		public DailyRule(Map<String, Object> config) {
			super(config);

			startHour = (int) ((Map) config.get("startDate")).get("hour");
			startMinute = (int) ((Map) config.get("startDate")).get("minute");

			endHour = (int) ((Map) config.get("endDate")).get("hour");
			endMinute = (int) ((Map) config.get("endDate")).get("minute");

			startTargets.put(Calendar.HOUR, startHour);
			startTargets.put(Calendar.MINUTE, startMinute);

			endTargets.put(Calendar.HOUR, endHour);
			endTargets.put(Calendar.MINUTE, endMinute);
		}
	}

	class WeeklyRule extends Rule {
		int startWeekday;
		int startHour;
		int startMinute;

		int endWeekday;
		int endHour;
		int endMinute;

		public WeeklyRule(Map<String, Object> config) {
			super(config);

			startWeekday = (int) ((Map) config.get("startDate")).get("weekday");
			startHour = (int) ((Map) config.get("startDate")).get("hour");
			startMinute = (int) ((Map) config.get("startDate")).get("minute");

			endWeekday = (int) ((Map) config.get("startDate")).get("weekday");
			endHour = (int) ((Map) config.get("endDate")).get("hour");
			endMinute = (int) ((Map) config.get("endDate")).get("minute");

			startTargets.put(Calendar.DAY_OF_WEEK, startWeekday);
			startTargets.put(Calendar.HOUR, startHour);
			startTargets.put(Calendar.MINUTE, startMinute);

			endTargets.put(Calendar.DAY_OF_WEEK, endWeekday);
			endTargets.put(Calendar.HOUR, endHour);
			endTargets.put(Calendar.MINUTE, endMinute);
		}
	}

	class MonthlyRule extends Rule {
		int startDate;
		int startHour;
		int startMinute;

		int endDate;
		int endHour;
		int endMinute;

		public MonthlyRule(Map<String, Object> config) {
			super(config);

			startDate = (int) ((Map) config.get("startDate")).get("date");
			startHour = (int) ((Map) config.get("startDate")).get("hour");
			startMinute = (int) ((Map) config.get("startDate")).get("minute");

			endDate = (int) ((Map) config.get("endDate")).get("date");
			endHour = (int) ((Map) config.get("endDate")).get("hour");
			endMinute = (int) ((Map) config.get("endDate")).get("minute");

			startTargets.put(Calendar.DATE, startDate);
			startTargets.put(Calendar.HOUR, startHour);
			startTargets.put(Calendar.MINUTE, startMinute);

			endTargets.put(Calendar.DATE, endDate);
			endTargets.put(Calendar.HOUR, endHour);
			endTargets.put(Calendar.MINUTE, endMinute);
		}
	}

	class YearlyRule extends Rule {
		int startMonth;
		int startDate;
		int startHour;
		int startMinute;

		int endMonth;
		int endDate;
		int endHour;
		int endMinute;

		public YearlyRule(Map<String, Object> config) {
			super(config);

			startMonth = (int) ((Map) config.get("startDate")).get("month");
			startDate = (int) ((Map) config.get("startDate")).get("date");
			startHour = (int) ((Map) config.get("startDate")).get("hour");
			startMinute = (int) ((Map) config.get("startDate")).get("minute");

			endMonth = (int) ((Map) config.get("endDate")).get("month");
			endDate = (int) ((Map) config.get("endDate")).get("date");
			endHour = (int) ((Map) config.get("endDate")).get("hour");
			endMinute = (int) ((Map) config.get("endDate")).get("minute");

			startTargets.put(Calendar.MONTH, startMonth);
			startTargets.put(Calendar.DATE, startDate);
			startTargets.put(Calendar.HOUR, startHour);
			startTargets.put(Calendar.MINUTE, startMinute);

			endTargets.put(Calendar.MONTH, endMonth);
			endTargets.put(Calendar.DATE, endDate);
			endTargets.put(Calendar.HOUR, endHour);
			endTargets.put(Calendar.MINUTE, endMinute);
		}
	}
}
