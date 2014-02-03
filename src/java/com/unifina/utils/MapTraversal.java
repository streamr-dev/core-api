package com.unifina.utils;

import java.util.Map;

public class MapTraversal {
	
	
	@SuppressWarnings("rawtypes")
	public static Object getProperty(Map map, String name) {		
		if (name==null)
			throw new IllegalArgumentException("Name can not be null!");
		if (map==null)
			return null;
		
		Object result = null;
		
		String[] names = name.split("\\.");
		for (int i=0;i<names.length;i++) {
			String s = names[i];
			result = map.get(s);
			if (result==null) 
				return null;
			// Not the last entry: result should be a Map
			if (i<names.length-1) {
				map = (Map) result;
			}
		}
		return result;
	}
	
	public static String getString(Map map, String name) {
		Object raw = getProperty(map,name);
		return (raw==null ? null : raw.toString());
	}
	
	public static Integer getInteger(Map map, String name) {
		Object raw = getProperty(map,name);
		if (raw==null) return null;
		else if (raw instanceof Integer)
			return (Integer) raw;
		else return Integer.parseInt(raw.toString());
	}

	public static Long getLong(Map map, String name) {
		Object raw = getProperty(map,name);
		if (raw==null) return null;
		else if (raw instanceof Long)
			return (Long) raw;
		else return Long.parseLong(raw.toString());
	}
	
	public static Double getDouble(Map map, String name) {
		Object raw = getProperty(map,name);
		if (raw==null) return null;
		else if (raw instanceof Double)
			return (Double) raw;
		else return Double.parseDouble(raw.toString());
	}
	
	public static Boolean getBoolean(Map map, String name) {
		Object raw = getProperty(map,name);
		if (raw==null) return false;
		else if (raw instanceof Boolean)
			return (Boolean) raw;
		else return Boolean.parseBoolean(raw.toString());
	}
}
