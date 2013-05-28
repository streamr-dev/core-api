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
	
	public static Integer getInteger(Map map, String name) {
		Object raw = getProperty(map,name);
		if (raw==null) return null;
		if (raw instanceof Integer)
			return (Integer) raw;
		else return Integer.parseInt(raw.toString());
	}
	
	public static Double getDouble(Map map, String name) {
		Object raw = getProperty(map,name);
		if (raw==null) return null;
		if (raw instanceof Double)
			return (Double) raw;
		else return Double.parseDouble(raw.toString());
	}
	
	public static Boolean getBoolean(Map map, String name) {
		Object raw = getProperty(map,name);
		if (raw==null) return null;
		if (raw instanceof Double)
			return (Boolean) raw;
		else return Boolean.parseBoolean(raw.toString());
	}
}
