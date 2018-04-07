package com.unifina.signalpath.time;

import java.io.Serializable;
import java.util.*;

abstract class Rule implements Serializable {

	static Rule instantiateRule(Map<String, Object> ruleConfig, TimeZone timeZone) {
		int type = (int) ruleConfig.get("intervalType");
		switch (type) {
			case 0:
				return new HourlyRule(0, ruleConfig, timeZone);
			case 1:
				return new DailyRule(1, ruleConfig, timeZone);
			case 2:
				return new WeeklyRule(2, ruleConfig, timeZone);
			case 3:
				return new MonthlyRule(3, ruleConfig, timeZone);
			case 4:
				return new YearlyRule(4, ruleConfig, timeZone);
			default:
				throw new RuntimeException("Invalid intervalType: " + type);
		}
	}

	private static final List<Integer> DATE_FIELDS = Arrays.asList(
		Calendar.YEAR,
		Calendar.MONTH,
		Calendar.DATE,
		Calendar.HOUR_OF_DAY,
		Calendar.MINUTE,
		Calendar.SECOND,
		Calendar.MILLISECOND
	);

	private static final List<Integer> DAY_OF_WEEK_FIELDS = Arrays.asList(
		Calendar.YEAR,
		Calendar.MONTH,
		Calendar.WEEK_OF_YEAR,
		Calendar.DAY_OF_WEEK,
		Calendar.HOUR_OF_DAY,
		Calendar.MINUTE,
		Calendar.SECOND,
		Calendar.MILLISECOND
	);

	private final int intervalType;
	private final Double value;
	private final Calendar cal;

	final LinkedHashMap<Integer, Integer> startTargets = new LinkedHashMap<>();
	final LinkedHashMap<Integer, Integer> endTargets = new LinkedHashMap<>();

	private Rule(int intervalType, Map<String, Object> config, TimeZone timeZone) {
		this.intervalType = intervalType;
		value = ((Number) config.get("value")).doubleValue();
		cal = Calendar.getInstance(timeZone);
	}

	Date getNext(Date now) {
		cal.setTime(now);
		if (isActive(now)) {
			return getNext(cal, endTargets);
		} else {
			return getNext(cal, startTargets);
		}
	}

	boolean isActive(Date now) {
		cal.setTime(now);
		Date nextStart = getNext(cal, startTargets);
		Date nextEnd = getNext(cal, endTargets);
		return nextEnd.before(nextStart);
	}

	Double getValue() {
		return value;
	}

	Map<String, Object> getConfig() {
		Map<String, Object> config = new HashMap<>();
		config.put("intervalType", intervalType);
		config.put("value", getValue());
		config.put("startDate", getStartDateConfig());
		config.put("endDate", getEndDateConfig());
		return config;
	}

	abstract Map<String, Integer> getStartDateConfig();

	abstract Map<String, Integer> getEndDateConfig();

	static Date getNext(Calendar cal, Map<Integer, Integer> targets) {
		cal = (Calendar) cal.clone();
		targets = new LinkedHashMap<>(targets);

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
					fields = DAY_OF_WEEK_FIELDS;
				} else {
					fields = DATE_FIELDS;
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

	private static class HourlyRule extends Rule {
		private final int startMinute;
		private final int endMinute;

		private HourlyRule(int intervalType, Map<String, Object> config, TimeZone timeZone) {
			super(intervalType, config, timeZone);

			startMinute = (int) ((Map) config.get("startDate")).get("minute");
			endMinute = (int) ((Map) config.get("endDate")).get("minute");

			startTargets.put(Calendar.MINUTE, startMinute);
			endTargets.put(Calendar.MINUTE, endMinute);
		}

		@Override
		Map<String, Integer> getStartDateConfig() {
			return Collections.singletonMap("minute", startMinute);
		}

		@Override
		Map<String, Integer> getEndDateConfig() {
			return Collections.singletonMap("minute", endMinute);
		}
	}

	private static class DailyRule extends Rule {
		private int startHour;
		private int startMinute;

		private int endHour;
		private int endMinute;

		private DailyRule(int intervalType, Map<String, Object> config, TimeZone timeZone) {
			super(intervalType, config, timeZone);

			startHour = (int) ((Map) config.get("startDate")).get("hour");
			startMinute = (int) ((Map) config.get("startDate")).get("minute");

			endHour = (int) ((Map) config.get("endDate")).get("hour");
			endMinute = (int) ((Map) config.get("endDate")).get("minute");

			startTargets.put(Calendar.HOUR_OF_DAY, startHour);
			startTargets.put(Calendar.MINUTE, startMinute);

			endTargets.put(Calendar.HOUR_OF_DAY, endHour);
			endTargets.put(Calendar.MINUTE, endMinute);
		}

		@Override
		Map<String, Integer> getStartDateConfig() {
			Map<String, Integer> config = new HashMap<>();
			config.put("hour", startHour);
			config.put("minute", startMinute);
			return config;
		}

		@Override
		Map<String, Integer> getEndDateConfig() {
			Map<String, Integer> config = new HashMap<>();
			config.put("hour", endHour);
			config.put("minute", endMinute);
			return config;
		}
	}

	private static class WeeklyRule extends Rule {
		private final int startWeekday;
		private final int startHour;
		private final int startMinute;

		private final int endWeekday;
		private final int endHour;
		private final int endMinute;

		private WeeklyRule(int intervalType, Map<String, Object> config, TimeZone timeZone) {
			super(intervalType, config, timeZone);

			startWeekday = (int) ((Map) config.get("startDate")).get("weekday");
			startHour = (int) ((Map) config.get("startDate")).get("hour");
			startMinute = (int) ((Map) config.get("startDate")).get("minute");

			endWeekday = (int) ((Map) config.get("endDate")).get("weekday");
			endHour = (int) ((Map) config.get("endDate")).get("hour");
			endMinute = (int) ((Map) config.get("endDate")).get("minute");

			startTargets.put(Calendar.DAY_OF_WEEK, startWeekday);
			startTargets.put(Calendar.HOUR_OF_DAY, startHour);
			startTargets.put(Calendar.MINUTE, startMinute);

			endTargets.put(Calendar.DAY_OF_WEEK, endWeekday);
			endTargets.put(Calendar.HOUR_OF_DAY, endHour);
			endTargets.put(Calendar.MINUTE, endMinute);
		}

		@Override
		Map<String, Integer> getStartDateConfig() {
			Map<String, Integer> config = new HashMap<>();
			config.put("weekday", startWeekday);
			config.put("hour", startHour);
			config.put("minute", startMinute);
			return config;
		}

		@Override
		Map<String, Integer> getEndDateConfig() {
			Map<String, Integer> config = new HashMap<>();
			config.put("weekday", endWeekday);
			config.put("hour", endHour);
			config.put("minute", endMinute);
			return config;
		}
	}


	private static class MonthlyRule extends Rule {
		private final int startDate;
		private final int startHour;
		private final int startMinute;

		private final int endDate;
		private final int endHour;
		private final int endMinute;

		private MonthlyRule(int intervalType, Map<String, Object> config, TimeZone timeZone) {
			super(intervalType, config, timeZone);

			startDate = (int) ((Map) config.get("startDate")).get("day");
			startHour = (int) ((Map) config.get("startDate")).get("hour");
			startMinute = (int) ((Map) config.get("startDate")).get("minute");

			endDate = (int) ((Map) config.get("endDate")).get("day");
			endHour = (int) ((Map) config.get("endDate")).get("hour");
			endMinute = (int) ((Map) config.get("endDate")).get("minute");

			startTargets.put(Calendar.DATE, startDate);
			startTargets.put(Calendar.HOUR_OF_DAY, startHour);
			startTargets.put(Calendar.MINUTE, startMinute);

			endTargets.put(Calendar.DATE, endDate);
			endTargets.put(Calendar.HOUR_OF_DAY, endHour);
			endTargets.put(Calendar.MINUTE, endMinute);
		}

		@Override
		Map<String, Integer> getStartDateConfig() {
			Map<String, Integer> config = new HashMap<>();
			config.put("day", startDate);
			config.put("hour", startHour);
			config.put("minute", startMinute);
			return config;
		}

		@Override
		Map<String, Integer> getEndDateConfig() {
			Map<String, Integer> config = new HashMap<>();
			config.put("day", endDate);
			config.put("hour", endHour);
			config.put("minute", endMinute);
			return config;
		}
	}

	private static class YearlyRule extends Rule {
		private final int startMonth;
		private final int startDate;
		private final int startHour;
		private final int startMinute;

		private final int endMonth;
		private final int endDate;
		private final int endHour;
		private final int endMinute;

		private YearlyRule(int intervalType, Map<String, Object> config, TimeZone timeZone) {
			super(intervalType, config, timeZone);

			startMonth = (int) ((Map) config.get("startDate")).get("month");
			startDate = (int) ((Map) config.get("startDate")).get("day");
			startHour = (int) ((Map) config.get("startDate")).get("hour");
			startMinute = (int) ((Map) config.get("startDate")).get("minute");

			endMonth = (int) ((Map) config.get("endDate")).get("month");
			endDate = (int) ((Map) config.get("endDate")).get("day");
			endHour = (int) ((Map) config.get("endDate")).get("hour");
			endMinute = (int) ((Map) config.get("endDate")).get("minute");

			startTargets.put(Calendar.MONTH, startMonth);
			startTargets.put(Calendar.DATE, startDate);
			startTargets.put(Calendar.HOUR_OF_DAY, startHour);
			startTargets.put(Calendar.MINUTE, startMinute);

			endTargets.put(Calendar.MONTH, endMonth);
			endTargets.put(Calendar.DATE, endDate);
			endTargets.put(Calendar.HOUR_OF_DAY, endHour);
			endTargets.put(Calendar.MINUTE, endMinute);
		}

		@Override
		Map<String, Integer> getStartDateConfig() {
			Map<String, Integer> config = new HashMap<>();
			config.put("month", startMonth);
			config.put("day", startDate);
			config.put("hour", startHour);
			config.put("minute", startMinute);
			return config;
		}

		@Override
		Map<String, Integer> getEndDateConfig() {
			Map<String, Integer> config = new HashMap<>();
			config.put("month", endMonth);
			config.put("day", endDate);
			config.put("hour", endHour);
			config.put("minute", endMinute);
			return config;
		}
	}
}