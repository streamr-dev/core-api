package com.unifina.domain.marketplace


import com.unifina.domain.security.User
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
