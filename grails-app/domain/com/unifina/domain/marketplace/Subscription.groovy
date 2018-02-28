package com.unifina.domain.marketplace

import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class Subscription {
	Long id
	String address
	Product product
	Date endsAt

	Date dateCreated
	Date lastUpdated

	static constraints = {
		address(unique: 'product', validator: Product.isEthereumAddress)
	}

	static mapping = {
		address(index: "address_idx")
	}

	SecUser getUser() {
		IntegrationKey.findByServiceAndIdInService(IntegrationKey.Service.ETHEREUM_ID, address)?.user
	}
}
