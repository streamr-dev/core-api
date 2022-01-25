package com.streamr.core.domain

import com.streamr.core.service.EthereumUserService
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import grails.util.Holders

@GrailsCompileStatic
@Entity
class SubscriptionPaid extends Subscription {

	String address

	static constraints = {
		address(unique: 'product', validator: EthereumAddressValidator.validate)
	}

	static mapping = {
		address(index: "address_idx")
	}

	@Override
	Map toMapInherited() {
		return [address: address]
	}

	@Override
	User fetchUser() {
		Holders.getApplicationContext().getBean(EthereumUserService).getEthereumUser(address)
	}
}
