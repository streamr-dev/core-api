package com.unifina.domain

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

		static State isState(String value) {
			if (value == null || value.trim().equals("")) {
				return null
			}
			try {
				return State.valueOf(value.toUpperCase())
			} catch (IllegalArgumentException e) {
				return null
			}
		}
	}

	String id
	// user requesting to join the data union.
	User user
	// memberAddress is an Ethereum address of the member requesting to join the data union.
	String memberAddress
	// contractAddress is the Ethereum address of the data union to join.
	String contractAddress
	// state of the join request.
	State state = State.PENDING
	Date dateCreated
	Date lastUpdated

	static constraints = {
		memberAddress(validator: EthereumAddressValidator.validate)
		contractAddress(validator: EthereumAddressValidator.validate)
	}
	static mapping = {
		id generator: IdGenerator.name
		state enumType: "ordinal", defaultValue: State.PENDING.ordinal(), index: 'state_idx'
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
