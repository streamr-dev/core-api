package com.unifina.domain.security

import com.unifina.domain.data.Feed
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.service.PermissionService
import com.unifina.utils.IdGenerator;

class SecUser {
	
	String username
	String password
	boolean enabled
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired
	
	String apiKey = IdGenerator.get()
	String apiSecret = IdGenerator.get()
	
	String name
	String timezone

	static hasMany = [permissions: Permission]
	
	static constraints = {
		username blank: false, unique: true, email: true
		password blank: false
		name blank: false
		apiKey nullable:true, unique: true
		apiSecret nullable:true
	}

	static mapping = {
		password column: '`password`'
		apiKey index: 'apiKey_index'
		permissions cascade: 'all-delete-orphan'
	}

	Set<SecRole> getAuthorities() {
		SecUserSecRole.findAllBySecUser(this).collect { it.secRole } as Set
	}
}
