package com.unifina.domain.signalpath

import com.unifina.domain.security.SecUser;

class SavedSignalPath {
	
	public static final Integer TYPE_USER_SIGNAL_PATH = 0
	public static final Integer TYPE_EXAMPLE_SIGNAL_PATH = 1
	
	Long id
	SecUser user
	String name
	String json
	Integer type
	
	Boolean hasExports
	
	Date dateCreated
	Date lastUpdated
	
    static constraints = {
    }
	
	static mapping = {
		json type: 'text'
		type defaultValue: "0"
	}

	@Override
	public String toString() {
		return "$name $id"
	}
		
}
