package com.streamr.core.domain

import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@GrailsCompileStatic
@Entity
class SubscriptionFree extends Subscription {
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
