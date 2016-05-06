package com.unifina.domain.security

import com.unifina.utils.IdGenerator;

class SecUser {
	
	String username
	String password
	boolean enabled
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired
	
	String apiKey = generateApiKey()
	
	String name
	String timezone

	static hasMany = [permissions: Permission]
	
	static constraints = {
		username blank: false, unique: true, email: true
		password blank: false
		name blank: false
		apiKey nullable:true, unique: true
	}

	static mapping = {
		password column: '`password`'
		apiKey index: 'apiKey_index'
		permissions cascade: 'all-delete-orphan'
	}

	Set<SecRole> getAuthorities() {
		SecUserSecRole.findAllBySecUser(this).collect { it.secRole } as Set
	}

	public Map toMap() {
		return [
			name           : name,
			username       : username,
			apiKey         : apiKey,
			timezone       : timezone,
		]
	}

	public static String generateApiKey() {
		return IdGenerator.get() + IdGenerator.get()
	}
}
