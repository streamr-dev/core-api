package com.unifina.domain.data

import com.unifina.domain.security.SecUser
import groovy.json.JsonSlurper

class Stream implements Comparable {
	Long id
	String uuid
	String apiKey
	SecUser user

	String name
	Feed feed
	String config
	String description
	
	Date firstHistoricalDay
	Date lastHistoricalDay

	static constraints = {
		name(blank:false)
		
		config(nullable:true)
		description(nullable:true)
		firstHistoricalDay(nullable:true)
		lastHistoricalDay(nullable:true)
		uuid(nullable:true)
		apiKey(nullable:true)
		user(nullable:true)
	}
	
	static mapping = {
		name index:"name_idx"
		uuid index: "uuid_idx"
		feed lazy:false
		config type: 'text'
	}
	
	@Override
	public String toString() {
		return name
	}

	def toMap() {
		[
			id: id,
			uuid: uuid,
			apiKey: apiKey,
			name: name,
			feedId: feed.id,
			config: config == null || config.empty ? config : new JsonSlurper().parseText(config),
			description: description
		]
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
}
