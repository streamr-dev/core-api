package com.unifina.domain.security

import com.unifina.security.Userish
import com.unifina.utils.IdGenerator
import grails.persistence.Entity
import groovy.transform.CompileStatic

/**
 * Key that either
 * 	(1) authenticates a <code>User</code> or
 * 	(2) acts as an 'anonymous' key that has permissions of its own
 */
@Entity
class Key implements Userish {
	String id
	String name
	User user

	static constraints = {
		user(nullable: true)
	}

	static mapping = {
		table '`key`'
		id generator: IdGenerator.name // Note: doesn't apply in unit tests
	}

	static hasMany = [permissions: Permission]

	@Override
	boolean equals(Object obj) {
		if (obj instanceof Key) {
			if (obj.id == null || this.id == null) {
				return this.is(obj)
			} else {
				return obj.id == this.id
			}
		} else if (obj instanceof String) {
			return obj == this.id
		} else {
			return false
		}
	}

	@CompileStatic
	Map toMap() {
		return [
			id: id,
			name: name,
			user: user?.username
		]
	}

	@Override
	Userish resolveToUserish() {
		return user ?: this
	}
}
