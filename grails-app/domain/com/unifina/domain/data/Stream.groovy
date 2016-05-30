package com.unifina.domain.data

import com.unifina.domain.security.SecUser
import com.unifina.utils.IdGenerator
import grails.converters.JSON
import groovy.transform.CompileStatic

class Stream implements Comparable {
	String id
	String apiKey
	SecUser user

	String name
	Feed feed
	String config
	String description
	
	Date firstHistoricalDay
	Date lastHistoricalDay

	Date dateCreated
	Date lastUpdated

	static constraints = {
		name(blank:false)
		config(nullable:true)
		description(nullable:true)
		firstHistoricalDay(nullable:true)
		lastHistoricalDay(nullable:true)
		apiKey(nullable:true)
		user(nullable:true)
	}
	
	static mapping = {
		id generator: 'assigned'
		name index:"name_idx"
		feed lazy:false
		config type: 'text'
	}
	
	@Override
	public String toString() {
		return name
	}

	@CompileStatic
	Map toMap() {
		[
			id: id,
			apiKey: apiKey,
			name: name,
			feed: feed.toMap(),
			config: config == null || config.empty ? config : JSON.parse(config),
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

	public Map<String, Object> getStreamConfigAsMap() {
		if (config!=null)
			return ((Map)JSON.parse(config));
		else return [:]
	}
}
