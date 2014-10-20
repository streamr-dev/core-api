package com.unifina.domain.security

import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedUser
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.domain.signalpath.ModulePackageUser

class SecUser {
	String username
	String password
	boolean enabled
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired

	String dataToken
	
	// Added by Unifina
	String timezone
	
	static constraints = {
		username blank: false, unique: true
		password blank: false
	}

	static mapping = {
		password column: '`password`'
	}

	Set<SecRole> getAuthorities() {
		SecUserSecRole.findAllBySecUser(this).collect { it.secRole } as Set
	}

	// Added by Unifina
	Set<ModulePackage> getModulePackages() {
		ModulePackageUser.findAllByUser(this).collect { it.modulePackage } as Set
	}
	
	// Added by Unifina
	Set<Feed> getFeeds() {
		FeedUser.findAllByUser(this).collect { it.feed } as Set
	}
}
