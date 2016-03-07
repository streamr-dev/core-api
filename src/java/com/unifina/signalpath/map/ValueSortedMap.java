package com.unifina.signalpath.map;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * An implementation of {@link java.util.Map} that keeps entries sorted primarily by value (descending) and secondarily
 * by key (ascending).
 *
 * @param <K> key
 * @param <V> value
 */
public class ValueSortedMap<K extends Comparable<K>, V extends Comparable<V>> extends TreeMap<K, V> {

	// For serialization
	@SuppressWarnings("unused")
	public ValueSortedMap() {
		this(false);
	}

	public ValueSortedMap(boolean descending) {
		super(new ValueComparator<K, V>(descending));
	}

	@Override
	public V put(K key, V value) {
		super.remove(key);
		innerHashMap().put(key, value);
		return super.put(key, value);
	}

	@Override
	public V remove(Object key) {
		V v = super.remove(key);
		innerHashMap().remove(key);
		return v;
	}

	@SuppressWarnings("unchecked")
	private Map<K, V> innerHashMap() {
		return ((ValueComparator<K, V>) comparator()).hashMap;
	}

	private static class ValueComparator<K extends Comparable<K>, V extends Comparable<V>> implements Comparator<K>, Serializable {
		final Map<K, V> hashMap = new HashMap<>();
		final Ordering<V> valueOrdering;

		public ValueComparator(boolean descending) {
			valueOrdering = descending ? Ordering.natural().<V>reverse() : Ordering.<V>natural();
		}

		@Override
		public int compare(K k1, K k2) {
			V v1 = hashMap.get(k1);
			V v2 = hashMap.get(k2);

			if (v1 == null) {
				return v2 == null ? 0 : 1;
			}

			return ComparisonChain.start()
				.compare(v1, v2, valueOrdering)
				.compare(k1, k2)
				.result();
		}
	}
}
