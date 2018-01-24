package com.unifina.domain.security

import com.unifina.utils.IdGenerator

class Challenge {
	String id
	String challenge
	static constraints = {
		id(primaryKeyName: "id")
		challenge(blank: false, nullable: false)
	}
	static mapping = {
		id generator: IdGenerator.name
	}
}
