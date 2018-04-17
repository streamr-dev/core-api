package com.unifina.domain.marketplace

import com.unifina.domain.security.SecUser
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class FreeSubscription extends Subscription {
	SecUser user

	static constraints = {
		user(unique: 'product')
	}

	@Override
	Map toMapInherited() {
		return [user: user.username]
	}

	@Override
	SecUser fetchUser() {
		return user
	}
}
