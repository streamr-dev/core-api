package com.unifina.domain.marketplace

import com.unifina.domain.security.SecUser
import com.unifina.service.EthereumIntegrationKeyService
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import grails.util.Holders

@GrailsCompileStatic
@Entity
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
		Holders.getApplicationContext().getBean(EthereumIntegrationKeyService).getEthereumUser(address)
	}
}
