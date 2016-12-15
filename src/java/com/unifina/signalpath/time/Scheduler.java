package com.unifina.signalpath.time;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.unifina.datasource.ITimeListener;
import com.unifina.domain.signalpath.Module;
import com.unifina.signalpath.ModuleWithUI;
import com.unifina.signalpath.TimeSeriesOutput;

public class Scheduler extends ModuleWithUI implements ITimeListener {

	TimeSeriesOutput out = new TimeSeriesOutput(this, "value");

	Date nextTime;
	List<Rule> rules = new ArrayList<Rule>();
	Double defaultValue;
	List<Integer> activeRules = new ArrayList<>();

	@Override
	public void init() {
		addOutput(out);
		resendAll = false;
		resendLast = 1;
	}

	public void clearState() {
	}

	public void sendOutput() {
	}

	@Override
	public void setTime(Date time) {
		if (nextTime == null){
			nextTime = getMinimumNextTime(time);
		}

		if (out.getValue() == null || time.equals(nextTime)) {
			boolean foundActive = false;
			int i = 0;
			activeRules.clear();
			for(Rule r : rules){
				if(r.isActive(time)){
					if(!foundActive){
						foundActive = true;
						out.send(r.getValue());
						nextTime = getMinimumNextTime(time);
					}
					activeRules.add(i);
				}
				i++;
			}
			if(!foundActive)
				out.send(defaultValue);
			
			if (getGlobals().getUiChannel()!=null) {
				Map<String,Object> msg = new HashMap<>();
				msg.put("activeRules", activeRules);
				getGlobals().getUiChannel().push(msg, uiChannelId);
			}
		}
	}

	private Date getMinimumNextTime(Date time) {
		Date firstNextTime = null;
		for(Rule r : rules){
			Date next = r.getNext(time);
			if(firstNextTime == null || next.before(firstNextTime)){
				firstNextTime = next;
			}
		}
		return firstNextTime;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		Module m = this.getDomainObject();
		Map<String, Object> schedule = (Map<String, Object>)config.get("schedule");
		
		if(schedule != null){
			List<Map<String, Object>> ruleList = (List<Map<String, Object>>) schedule.get("rules");
			defaultValue = ((Number)schedule.get("defaultValue")).doubleValue();
			// Read json rules and add Rule objects to list
			rules.clear();
			for (Map<String, Object> rule : ruleList) {
				int type = (int) rule.get("intervalType");
				Rule r;
				if(type == 0)
					r = new HourlyRule(rule);
				else if(type == 1)
					r = new DailyRule(rule);
				else if(type == 2)
					r = new WeeklyRule(rule);
				else if(type == 3)
					r = new MonthlyRule(rule);
				else if (type == 4)
					r = new YearlyRule(rule);
				else
					r = new Rule();
				r.setTimeZone(getGlobals().getUser().getTimezone());
				rules.add(r);
			}
		}
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		// Read List of Rule objects and create json rules
		ArrayList<Map<String, Object>> ruleList = new ArrayList<>();
		for (Rule r : rules) {
			ruleList.add(r.getSchedule());
		}

		config.put("rules", ruleList);
		config.put("defaultValue", defaultValue);
		
		return config;
	}
	
	public List<Rule> getRules(){
		return rules;
	}
	
	public Rule getRule(int index){
		return rules.get(index);
	}

	public class Rule {
		private Map<String, Object> schedule;

		private Double value;

		private List<Integer> dateFields = Arrays.asList(new Integer[] {
				Calendar.YEAR, Calendar.MONTH, Calendar.DATE, Calendar.HOUR_OF_DAY, Calendar.MINUTE,
				Calendar.SECOND, Calendar.MILLISECOND });
		private List<Integer> dayOfWeekFields = Arrays.asList(new Integer[] {
				Calendar.YEAR, Calendar.MONTH, Calendar.WEEK_OF_YEAR, Calendar.DAY_OF_WEEK,
				Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND,
				Calendar.MILLISECOND });
		
		private Calendar cal = Calendar.getInstance();

		public HashMap<Integer, Integer> startTargets = new HashMap<>();
		public HashMap<Integer, Integer> endTargets = new HashMap<>();
		
		public Rule() {}
		
		public void setSchedule(Map<String, Object> schedule){
			this.schedule = schedule;
			value = ((Number)schedule.get("value")).doubleValue();
		}

		public Date getNext(Date now) {
			if (this.isActive(now)) {
				return getNext(now, endTargets);
			} else {
				return getNext(now, startTargets);
			}
		}

		public boolean isActive(Date now) {
			Date nextStart = getNext(now, startTargets);
			Date nextEnd = getNext(now, endTargets);
			return nextEnd.before(nextStart);
		}

		public Double getValue() {
			return value;
		}

		public Map<String, Object> getSchedule() {
			return schedule;
		}
		
		public void setTimeZone(TimeZone tz){
			cal.setTimeZone(tz);
		}
		
		public void setTimeZone(String tz){
			cal.setTimeZone(TimeZone.getTimeZone(tz));
		}

		private Date getNext(Date now, Map<Integer, Integer> t) {
			cal.setTime(now);
			
			Map<Integer, Integer> targets = new HashMap<Integer, Integer>(t);

			int target;
			List<Integer> fields = null;
			Integer[] targetFields = Arrays.copyOf(targets.keySet().toArray(),
					targets.keySet().toArray().length, Integer[].class);

			for (int i = 0; i < targets.size(); i++) {
				int field = targetFields[i];
				target = targets.get(field);

				int valueNow = cal.get(field);

				if (fields == null) {
					if (field == Calendar.DAY_OF_WEEK) {
						fields = dayOfWeekFields;
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
							targets.remove(fields.get(j));
						} else {
							cal.set(fields.get(j), 0);
						}
					}
				} else if (field == Calendar.MINUTE && valueNow == target) {
					int fieldToRaise = fields.get(fields
							.indexOf(targetFields[0]) - 1);
					cal.add(fieldToRaise, 1);
					for (int j = fields.indexOf(field); j < fields.size(); j++) {
						if (targets.containsKey(fields.get(j))) {
							cal.set(fields.get(j), targets.get(fields.get(j)));
							targets.remove(fields.get(j));
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

		public HourlyRule(Map<String, Object> schedule) {
			setSchedule(schedule);

			startMinute = (int) ((Map) schedule.get("startDate")).get("minute");

			endMinute = (int) ((Map) schedule.get("endDate")).get("minute");

			startTargets.put(Calendar.MINUTE, startMinute);

			endTargets.put(Calendar.MINUTE, endMinute);
		}
	}

	class DailyRule extends Rule {
		int startHour;
		int startMinute;

		int endHour;
		int endMinute;

		public DailyRule(Map<String, Object> schedule) {
			setSchedule(schedule);

			startHour = (int) ((Map) schedule.get("startDate")).get("hour");
			startMinute = (int) ((Map) schedule.get("startDate")).get("minute");

			endHour = (int) ((Map) schedule.get("endDate")).get("hour");
			endMinute = (int) ((Map) schedule.get("endDate")).get("minute");

			startTargets.put(Calendar.HOUR_OF_DAY, startHour);
			startTargets.put(Calendar.MINUTE, startMinute);

			endTargets.put(Calendar.HOUR_OF_DAY, endHour);
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

		public WeeklyRule(Map<String, Object> schedule) {
			setSchedule(schedule);

			startWeekday = (int) ((Map) schedule.get("startDate")).get("weekday");
			startHour = (int) ((Map) schedule.get("startDate")).get("hour");
			startMinute = (int) ((Map) schedule.get("startDate")).get("minute");

			endWeekday = (int) ((Map) schedule.get("endDate")).get("weekday");
			endHour = (int) ((Map) schedule.get("endDate")).get("hour");
			endMinute = (int) ((Map) schedule.get("endDate")).get("minute");

			startTargets.put(Calendar.DAY_OF_WEEK, startWeekday);
			startTargets.put(Calendar.HOUR_OF_DAY, startHour);
			startTargets.put(Calendar.MINUTE, startMinute);

			endTargets.put(Calendar.DAY_OF_WEEK, endWeekday);
			endTargets.put(Calendar.HOUR_OF_DAY, endHour);
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

		public MonthlyRule(Map<String, Object> schedule) {
			setSchedule(schedule);

			startDate = (int) ((Map) schedule.get("startDate")).get("day");
			startHour = (int) ((Map) schedule.get("startDate")).get("hour");
			startMinute = (int) ((Map) schedule.get("startDate")).get("minute");

			endDate = (int) ((Map) schedule.get("endDate")).get("day");
			endHour = (int) ((Map) schedule.get("endDate")).get("hour");
			endMinute = (int) ((Map) schedule.get("endDate")).get("minute");

			startTargets.put(Calendar.DATE, startDate);
			startTargets.put(Calendar.HOUR_OF_DAY, startHour);
			startTargets.put(Calendar.MINUTE, startMinute);

			endTargets.put(Calendar.DATE, endDate);
			endTargets.put(Calendar.HOUR_OF_DAY, endHour);
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

		public YearlyRule(Map<String, Object> schedule) {
			setSchedule(schedule);

			startMonth = (int) ((Map) schedule.get("startDate")).get("month");
			startDate = (int) ((Map) schedule.get("startDate")).get("day");
			startHour = (int) ((Map) schedule.get("startDate")).get("hour");
			startMinute = (int) ((Map) schedule.get("startDate")).get("minute");

			endMonth = (int) ((Map) schedule.get("endDate")).get("month");
			endDate = (int) ((Map) schedule.get("endDate")).get("day");
			endHour = (int) ((Map) schedule.get("endDate")).get("hour");
			endMinute = (int) ((Map) schedule.get("endDate")).get("minute");

			startTargets.put(Calendar.MONTH, startMonth);
			startTargets.put(Calendar.DATE, startDate);
			startTargets.put(Calendar.HOUR_OF_DAY, startHour);
			startTargets.put(Calendar.MINUTE, startMinute);

			endTargets.put(Calendar.MONTH, endMonth);
			endTargets.put(Calendar.DATE, endDate);
			endTargets.put(Calendar.HOUR_OF_DAY, endHour);
			endTargets.put(Calendar.MINUTE, endMinute);
		}
	}
}
