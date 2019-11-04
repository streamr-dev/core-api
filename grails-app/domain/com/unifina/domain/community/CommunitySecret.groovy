package com.unifina.domain.community

import com.unifina.domain.marketplace.Product
import com.unifina.utils.IdGenerator
import grails.compiler.GrailsCompileStatic
import groovy.transform.ToString

@ToString
class CommunitySecret {
	String id
	// name to display for users.
	String name
	// secret shared by the community that enables automatic join.
	String secret
	// communityAddress is an Ethereum address of the community.
	String communityAddress

    static constraints = {
		communityAddress(validator: Product.isEthereumAddress)
    }
	static mapping = {
		id generator: IdGenerator.name
	}

	@GrailsCompileStatic
	Map toMap() { [
		id: id,
		name: name,
		communityAddress: communityAddress,
		secret: secret,
	] }

}
