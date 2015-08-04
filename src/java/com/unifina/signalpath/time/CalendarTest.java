package com.unifina.signalpath.time;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarTest {
	private static List<Integer> dateFields = Arrays.asList(new Integer[] {
			Calendar.YEAR, Calendar.MONTH, Calendar.DATE, Calendar.HOUR_OF_DAY, Calendar.MINUTE,
			Calendar.SECOND, Calendar.MILLISECOND });
	private static List<Integer> dayOfWeekFields = Arrays.asList(new Integer[] {
			Calendar.YEAR, Calendar.MONTH, Calendar.WEEK_OF_YEAR, Calendar.DAY_OF_WEEK, Calendar.HOUR_OF_DAY, Calendar.MINUTE,
			Calendar.SECOND, Calendar.MILLISECOND });
	private static Calendar cal = Calendar.getInstance();
	
	public static void main(String[] args){
		Date now = new Date();
		Map<Integer, Integer> targets = new HashMap<>();
		
		targets.put(Calendar.MINUTE, 49);
		
		Date next = getNext(now, targets);
		
		System.out.println(now);
		System.out.println(next);
	}
	
	private static Date getNext(Date now, Integer field, Integer target) {
		if(field == Calendar.HOUR)
			field = Calendar.HOUR_OF_DAY;
		
		cal.setTime(now);
		
		List<Integer> fields;
		int valueNow = cal.get(field);
		
		if(field == Calendar.DAY_OF_WEEK){
			fields = dayOfWeekFields;
		} else {
			fields = dateFields;
		}
		
		if(valueNow > target){
			cal.add(fields.get(fields.indexOf(field)-1),1);
		}
		cal.set(field, target);
		
		int i = fields.indexOf(field);
		
		for(int f = i+1; f < fields.size(); f++){
			int newField = fields.get(f);
			cal.set(newField, cal.getMinimum(newField));
		}
		
		return cal.getTime();
	}

	private static Date getNext(Date now, Map<Integer, Integer> targets){
		cal.setTime(now);
		
		Date date = now;
		int target;
		List<Integer> fields = null;
		Integer[] targetFields = Arrays.copyOf(targets.keySet().toArray(), targets.keySet().toArray().length, Integer[].class);
		
		for(int i = 0; i < targets.size(); i++){
			int field = targetFields[i];
			target = targets.get(field);
			if(field == Calendar.HOUR)
				field = Calendar.HOUR_OF_DAY;
			
			int valueNow = cal.get(field);
			
			if(fields == null){
				if(field == Calendar.DAY_OF_WEEK){
					fields = dayOfWeekFields;
				} else {
					fields = dateFields;
				}
			}
			
			if(valueNow != target){
				if(valueNow > target){
					int fieldToRaise = fields.get(fields.indexOf(targetFields[0])-1);
					cal.add(fieldToRaise,1);
				}
				for(int j = fields.indexOf(field); j < fields.size(); j++){
					if(targets.containsKey(fields.get(j))){
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
