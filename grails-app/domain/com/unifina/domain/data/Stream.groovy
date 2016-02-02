package com.unifina.domain.data

import com.unifina.domain.security.SecUser
import grails.converters.JSON

class Stream implements Comparable {
	Long id
	String uuid
	String apiKey
	SecUser user
	
	String name
	Feed feed
	String streamConfig
	// An id local to the Feed

	@Deprecated
	String localId
	String description
	
	Date firstHistoricalDay
	Date lastHistoricalDay
	
	static constraints = {
		name(blank:false)
		localId(nullable:true)
		streamConfig(nullable:true)
		description(nullable:true)
		firstHistoricalDay(nullable:true)
		lastHistoricalDay(nullable:true)
		uuid(nullable:true)
		apiKey(nullable:true)
		user(nullable:true)
	}
	
	static mapping = {
		name index:"name_idx"
		localId index: 'localId_idx'
		uuid index: "uuid_idx"
		feed lazy:false
		streamConfig type: 'text'
	}
	
	@Override
	public String toString() {
		return name
	}
	
	@Override
	public int compareTo(Object arg0) {
		if (!(arg0 instanceof Stream)) return 0
		else return arg0.name.compareTo(this.name)
	}
	
	@Override
	public int hashCode() {
		return id.hashCode()
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof Stream && obj.id == this.id
	}

	public Map<String, Object> getStreamConfigAsMap() {
		if (streamConfig!=null)
			return ((Map)JSON.parse(streamConfig));
		else return [:]
	}

}
