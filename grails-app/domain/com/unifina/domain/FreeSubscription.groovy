package com.unifina.domain

import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@GrailsCompileStatic
@Entity
class FreeSubscription extends Subscription {
	User user

	static constraints = {
		user(unique: 'product')
	}

	@Override
	Map toMapInherited() {
		return [user: user.username]
	}

	@Override
	User fetchUser() {
		return user
	}
}
