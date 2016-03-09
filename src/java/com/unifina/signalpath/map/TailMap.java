package com.unifina.signalpath.map;

import java.util.*;

public class TailMap extends LimitMap {
	@Override
	protected Map makeLimitedCopyOfMap(Map source, Integer limit) {
		if (source.isEmpty()) {
			return source;
		} else if (source instanceof NavigableMap) {
			return handleNavigableMap((NavigableMap) source, limit);
		} else {
			return handleMap(source, limit);
		}
	}

	/**
	 * More efficient variant of handleMap()
	 */
	private static Map handleNavigableMap(NavigableMap navigableSource, Integer limit) {
		Iterator it = navigableSource.descendingKeySet().iterator();
		int i = 0;
		Object key = null;
		while (i < limit && it.hasNext()) {
			key = it.next();
			++i;
		}
		return navigableSource.tailMap(key);
	}

	private static Map handleMap(Map source, Integer limit) {
		Map<Object, Object> target = new LinkedHashMap<>(limit);
		int i = source.size();
		for (Object key : source.keySet()) {
			if (i-- > limit) {
				continue;
			}
			target.put(key, source.get(key));
		}
		return target;
	}
}
