package com.unifina.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MapTraversal {

	/**
	 * Fetch given property in the nested map tree,
	 * @param name e.g. "options.inputs[3].value"
	 * @return given property, or null if not found
	 */
	@SuppressWarnings("rawtypes")
	public static Object getProperty(Map map, String name) {
		if (name == null) { throw new IllegalArgumentException("Name can not be null!"); }

		// split with -1 limit also returns trailing empty strings (so that malformed names won't accidentally work)
		Object i = map;
		for (String prop : name.split("\\.", -1)) {
			if (!(i instanceof Map)) {
				if (i instanceof List && (prop.equalsIgnoreCase("count") || prop.equalsIgnoreCase("length") || prop.equalsIgnoreCase("size"))) {
					return new Integer(((List)i).size());
				}
				return null;
			}
			// iterate through (possibly nested) List property, e.g. "inputs[3]"
			// Note: if prop isn't a List, then simply listParts[0] == prop, and loop is skipped
			String[] listParts = prop.split("\\[", -1);
			i = ((Map)i).get(listParts[0]);
			for (int j = 1; j < listParts.length; j++) {
				if (!(i instanceof List)) { return null; }
				int len = listParts[j].length();
				if (len < 2 || listParts[j].charAt(len-1) != ']') {
					return null;    // malformed, e.g. "inputs[13.value" or "inputs[[3]].value" or "inputs[hax[2]].value"
				}
				try {
					int index = Integer.parseInt(listParts[j].substring(0, len - 1));
					i = ((List)i).get(index);
				} catch (Exception e) {
					return null;
				}
			}
		}
		return i;
	}
	
	public static String getString(Map map, String name) {
		Object raw = getProperty(map,name);
		return (raw==null ? null : raw.toString());
	}

	public static String getString(Map map, String name, String defaultValue) {
		Object raw = getProperty(map,name);
		return (raw==null ? defaultValue : raw.toString());
	}

	public static Integer getInteger(Map map, String name) {
		Object raw = getProperty(map,name);
		if (raw==null) return null;
		else if (raw instanceof Integer)
			return (Integer)raw;
		else return Integer.parseInt(raw.toString());
	}

	/**
	 * @throws NumberFormatException if value wasn't parseable to integer (e.g. "foo", 3.5)
	 * @throws NullPointerException if value wasn't found
     */
	public static int getInt(Map map, String name) throws NumberFormatException, NullPointerException {
		Object raw = getProperty(map, name);
		if (raw instanceof Number) {
			return ((Number)raw).intValue();
		} else {
			return Integer.parseInt(raw.toString());
		}
	}

	public static int getInt(Map map, String name, int defaultValue) {
		try {
			return getInt(map, name);
		} catch (NumberFormatException | NullPointerException e) {
			return defaultValue;
		}
	}

	public static Long getLong(Map map, String name) {
		Object raw = getProperty(map,name);
		if (raw==null) return null;
		else if (raw instanceof Number)
			return ((Number) raw).longValue();
		else return Long.parseLong(raw.toString());
	}
	
	public static Double getDouble(Map map, String name) {
		Object raw = getProperty(map,name);
		if (raw==null) return null;
		else if (raw instanceof Number)
			return ((Number) raw).doubleValue();
		else return Double.parseDouble(raw.toString());
	}

	public static Boolean getBoolean(Map map, String name, boolean defaultValue) {
		Object raw = getProperty(map,name);
		if (raw == null) {
			return defaultValue;
		} else if (raw instanceof Boolean) {
			return (Boolean) raw;
		} else {
			return Boolean.parseBoolean(raw.toString());
		}
	}

	public static Boolean getBoolean(Map map, String name) {
		return getBoolean(map, name, false);
	}
	
	public static Date getDate(Map map, String name, SimpleDateFormat df) {
		Object raw = getProperty(map,name);
		if (raw==null) return null;
		else if (raw instanceof Date)
			return (Date) raw;
		else
			try {
				return df.parse(raw.toString());
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
	}
	
	public static Map getMap(Map map, String name) {
		return (Map) getProperty(map,name);
	}
	
	public static List getList(Map map, String name) {
		return (List) getProperty(map,name);
	}
	
	public static Object[] getArray(Map map, String name) {
		return (Object[]) getProperty(map,name);
	}
	
	public static Map<String,Object> flatten(Map<String,Object> map) {
		Map<String,Object> result = new LinkedHashMap<>();
		return flattenRecursive(map, "", result);
	}
	
	private static Map<String,Object> flattenRecursive(Map<String,Object> map, String prefix, Map<String,Object> result) {
		for (String s : map.keySet()) {
			Object value = map.get(s);
			if (!(value instanceof Map)) {
				result.put(prefix+s,value);
			}
			else flattenRecursive((Map)value, prefix+s+".", result);
		}
		return result;
	}
}
