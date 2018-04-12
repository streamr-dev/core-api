package com.unifina.domain.marketplace

import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class PaidSubscription extends Subscription {
	String address

	static constraints = {
		address(unique: 'product', validator: Product.isEthereumAddress)
	}

	static mapping = {
		address(index: "address_idx")
	}

	@Override
	Map toMapInherited() {
		return [address: address]
	}

	@Override
	SecUser fetchUser() {
		IntegrationKey.findByServiceAndIdInService(IntegrationKey.Service.ETHEREUM_ID, address)?.user
	}
}
