package com.unifina.domain.signalpath

import com.unifina.domain.security.SecUser

class RunningSignalPath {
	SecUser user
	
	String runner
	String server
	String json
	
	Date dateCreated
	Date lastUpdated
	
	static hasMany = [uiChannels: UiChannel]
	
	static mapping = {
		json type: 'text'
		runner index: 'runner_idx'
		uiChannels cascade: 'all-delete-orphan'
	}
	
}
