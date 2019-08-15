package com.unifina.utils

class PropertiesUtil {
	/**
	 * Returns a Map of Properties matching a prefix. The keys in the Map
	 * will be the name of the Property without the prefix, and the values will
	 * be the property values.
	 *
	 * For example, calling this method on the Properties [foo.bar: 1, foo.foo: 2, xyz: 3]
	 * and using the prefix "foo." will return the map [bar: 1, foo:2]
	 */
	static Map<String, Object> matchingPropertiesToMap(String prefix, Properties properties) {
		Map matchingProps = properties.findAll {key, val-> key.toString().startsWith(prefix)}
		if (matchingProps.isEmpty()) {
			return null
		} else {
			// Else collect system properties to Map
			return matchingProps.collectEntries { key, val ->
					[(key.toString().replace(prefix, "")): val]
				}
		}
	}
}
