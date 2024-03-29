package com.streamr.core.domain

import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@GrailsCompileStatic
@Entity
abstract class Subscription {
	Long id
    Product product
	Date endsAt

	Date dateCreated
	Date lastUpdated

	Map toMap() {
		return [
			endsAt: endsAt,
			dateCreated: dateCreated,
			lastUpdated: lastUpdated,
			product: product.toSummaryMap(),
		] + toMapInherited()
	}

	abstract Map toMapInherited()

	abstract User fetchUser()
}
