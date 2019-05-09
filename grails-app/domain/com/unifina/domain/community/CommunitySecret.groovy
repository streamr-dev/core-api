package com.unifina.domain.community

import com.unifina.domain.marketplace.Product
import com.unifina.utils.HexIdGenerator
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
		id generator: HexIdGenerator.name
	}

	@GrailsCompileStatic
	Map toMap() {
		// TODO:
		//  We probably need to be able to return the secret in the object?
		//  Especially if it's generated server-side.
		//  The API should of course only serve the secrets to the community admin.
		return [
			id: id,
			name: name,
			communityAddress: communityAddress,
		]
	}
}
