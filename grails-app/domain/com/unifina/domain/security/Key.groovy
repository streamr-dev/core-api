package com.unifina.domain.security

import com.unifina.utils.IdGenerator

/**
 * Key that either
 * 	(1) authenticates a <code>SecUser</code> or
 * 	(2) acts as an 'anonymous' key that has permissions of its own
 */
class Key {
	String id
	String name
	SecUser user

	static constraints = {
		user(nullable: true)
	}

	static mapping = {
		id generator: IdGenerator.name // Note: doesn't apply in unit tests
	}
}
