package com.unifina.domain.security

import grails.persistence.Entity

@Entity
class Role {

	String authority

	static mapping = {
		cache true
	}

	static constraints = {
		authority blank: false, unique: true
	}
}
