package com.unifina.atmosphere

import spock.lang.Specification

class CounterBroadcasterCacheSpec extends Specification {CounterBroadcasterCache cache
	def setup() {cache = new CounterBroadcasterCache()	}	def "the cache must contain all added messages"() {		when:		for (int i=0;i<100;i++)			cache.add("$i", i, null)		List range = cache.getCounterRange(0, 99)				then:		range.size()==100		range[0]=="0"		range[1]=="1"		range[99]=="99"
	}		def "the cache must remove the right messages"() {		when:		for (int i=0;i<100;i++)			cache.add("$i", i, null)		List range = cache.getCounterRange(0, 99)				then:		range[0]=="0"				when:		cache.removeUpToCounter(49)		List newRange = cache.getCounterRange(50, 99)				then: "the old list is untouched"		range[0]=="0"		range[99]=="99"		then:		newRange[0]=="50"		newRange[49]=="99"	}		def "the cache must replace messages with non-null cacheId"() {		when:		cache.add("foo", 0, "key")		cache.add("bar", 1, "key")		cache.add("xyzzy", 2, "key")				List range = cache.getCounterRange(0, 2)				then:		range[0]==cache.PURGED_STRING		range[1]==cache.PURGED_STRING		range[2]=="xyzzy"	}		def "the cache must not fail if the range requested has been removed"() {		when:		cache.add("foo", 0, "key")		cache.add("bar", 1, null)		cache.removeUpToCounter(0)		List range = cache.getCounterRange(0, 1)				then:		range.size()==1
	}		def "the cache must not fail if the item to be replaced is removed"() {		when:		cache.add("foo", 0, "key")		cache.add("bar", 1, null)		cache.removeUpToCounter(0)		cache.add("xyzzy", 2, "key")		List range = cache.getCounterRange(0, 2)				then:		range.size()==2		range[0]=="bar"		range[1]=="xyzzy"	}
}
