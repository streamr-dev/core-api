package com.unifina.domain.marketplace

import com.unifina.domain.security.SecUser
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
abstract class Subscription {
	Long id
	Product product
	Date endsAt

	Date dateCreated
	Date lastUpdated

	Map toMap() {
		return [
			endsAt: endsAt,
			product: product.toSummaryMap(),
		] + toMapInherited()
	}

	abstract Map toMapInherited()

	abstract SecUser fetchUser()
}
