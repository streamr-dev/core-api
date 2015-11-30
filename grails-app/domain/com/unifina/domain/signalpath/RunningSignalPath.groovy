package com.unifina.domain.signalpath

import com.unifina.domain.security.SecUser

class RunningSignalPath {
	Long id
	SecUser user
	String name
	String json
	
	String runner
	String server
	String requestUrl

	Date dateCreated
	Date lastUpdated
	
	Boolean shared
	Boolean adhoc
	String state
	String serialized
	Date serializationTime
	
	static hasMany = [uiChannels: UiChannel]
	
	static constraints = {
		runner(nullable:true)
		server(nullable:true)
		requestUrl(nullable:true)
		shared(nullable:true)
		state(nullable:true)
		adhoc(nullable:true)
		serialized(nullable:true)
		serializationTime(nullable: true)
	}
	
	static mapping = {
		json type: 'text'
		runner index: 'runner_idx'
		uiChannels cascade: 'all-delete-orphan'
		serialized type: 'text'
	}

	boolean isNotSerialized() {
		serialized == null || serialized.empty
	}
}
