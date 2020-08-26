package com.unifina.domain

import grails.persistence.Entity

@Entity
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
