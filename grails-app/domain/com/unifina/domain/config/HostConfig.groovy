package com.unifina.domain.config

class HostConfig implements Serializable {

	String host
	String parameter
	String value
	
    static constraints = {
    }
	
	static mapping = {
		id composite: ['host','parameter']
	}
}
