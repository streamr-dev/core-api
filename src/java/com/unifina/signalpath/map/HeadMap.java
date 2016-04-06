package com.unifina.signalpath.map;

import java.util.LinkedHashMap;
import java.util.Map;

public class HeadMap extends LimitMap {
	@Override
	protected Map makeLimitedCopyOfMap(Map source, Integer limit) {
		Map<Object, Object> target = new LinkedHashMap<>(limit);
		int i = 0;
		for (Object key : source.keySet()) {
			target.put(key, source.get(key));
			if (++i == limit) {
				break;
			}
		}
		return target;
	}
}
