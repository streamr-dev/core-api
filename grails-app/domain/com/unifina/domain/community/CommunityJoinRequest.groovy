package com.unifina.domain.community

import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.SecUser
import com.unifina.utils.IdGenerator
import grails.compiler.GrailsCompileStatic
import groovy.transform.ToString

@ToString
class CommunityJoinRequest {
	enum State {
		PENDING,
		ACCEPTED,
		REJECTED
	}

	String id
	// user requesting to join a community.
	SecUser user
	// memberAddress is an Ethereum address of the member requesting to join community.
	String memberAddress
	// communityAddress is an Ethereum address of the community to join.
	String communityAddress
	// state of the join request.
	State state = State.PENDING
	Date dateCreated
	Date lastUpdated

    static constraints = {
		memberAddress(validator: Product.isEthereumAddress)
		communityAddress(validator: Product.isEthereumAddress)
    }
	static mapping = {
		id generator: IdGenerator.name
		state enumType: "identity", defaultValue: State.PENDING, index: 'state_idx'
	}

	@GrailsCompileStatic
	Map toMap() {
		return [
		    id: id,
			memberAddress: memberAddress,
			communityAddress: communityAddress,
			state: state,
			dateCreated: dateCreated,
			lastUpdated: lastUpdated,
		]
	}
}
