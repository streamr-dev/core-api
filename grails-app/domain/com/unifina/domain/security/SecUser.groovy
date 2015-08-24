package com.unifina.domain.security

import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedUser
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.domain.signalpath.ModulePackageUser
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
	}

	Set<SecRole> getAuthorities() {
		SecUserSecRole.findAllBySecUser(this).collect { it.secRole } as Set
	}

	Set<ModulePackage> getModulePackages() {
		ModulePackageUser.findAllByUser(this).collect { it.modulePackage } as Set
	}
	
	Set<Feed> getFeeds() {
		FeedUser.findAllByUser(this).collect { it.feed } as Set
	}
}
