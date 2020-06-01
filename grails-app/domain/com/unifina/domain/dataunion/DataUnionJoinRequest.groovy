package com.unifina.domain.dataunion

import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.SecUser
import com.unifina.utils.IdGenerator
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import groovy.transform.ToString

@ToString
@Entity
class DataUnionJoinRequest {

	enum State {
		PENDING,
		ACCEPTED,
		REJECTED
	}

	String id
	// user requesting to join the data union.
	SecUser user
	// memberAddress is an Ethereum address of the member requesting to join the data union.
	String memberAddress
	// contractAddress is the Ethereum address of the data union to join.
	String contractAddress
	// state of the join request.
	State state = State.PENDING
	Date dateCreated
	Date lastUpdated

    static constraints = {
		memberAddress(validator: Product.isEthereumAddress)
		contractAddress(validator: Product.isEthereumAddress)
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
			contractAddress: contractAddress,
			state: state?.toString(),
			dateCreated: dateCreated,
			lastUpdated: lastUpdated,
		]
	}
}
