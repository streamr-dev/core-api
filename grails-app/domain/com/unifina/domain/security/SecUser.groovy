package com.unifina.domain.security

class SecUser {
	
	String username
	String password
	boolean enabled
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired
	
	String name
	String timezone

	static hasMany = [permissions: Permission]
	
	static constraints = {
		username blank: false, unique: true, email: true
		password blank: false
		name blank: false
	}

	static mapping = {
		password column: '`password`'
		permissions cascade: 'all-delete-orphan'
	}

	Set<SecRole> getAuthorities() {
		SecUserSecRole.findAllBySecUser(this).collect { it.secRole } as Set
	}

	Key getKey() {
		Key.findByUser(this)
	}

	Map toMap() {
		return [
			name           : name,
			username       : username,
			timezone       : timezone,
		]
	}
}
