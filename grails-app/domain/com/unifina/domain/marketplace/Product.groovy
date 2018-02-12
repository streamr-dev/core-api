package com.unifina.domain.marketplace

import com.unifina.domain.data.Stream
import com.unifina.utils.IdGenerator

class Product {
	String id
	String name
	String description
	String imageUrl

	Category category
	State state = State.NEW
	String tx
	Stream previewStream
	String previewConfigJson

	Date dateCreated
	Date lastUpdated

	// The below fields exist in the domain object for speed & query support, but the ground truth is in the smart contract.
	String ownerAddress
	String beneficiaryAddress
	Long pricePerSecond
	Currency priceCurrency = Currency.DATA
	Long minimumSubscriptionInSeconds = 0

	static hasMany = [streams: Stream]

	enum State {
		NEW("new"),
		DEPLOYING("deploying"),
		DEPLOYED("deployed"),
		DELETING("deleting"),
		DELETED("deleted")

		String id

		State(String id) {
			this.id = id
		}
	}

	enum Currency {
		DATA("DATA"),
		USD("USD")

		String id

		Currency(String id) {
			this.id = id
		}
	}

	static constraints = {
		imageUrl(nullable: true)
		tx(nullable: true)
		previewStream(nullable: true)
		previewConfigJson(nullable: true)
	}

	static mapping = {
		id generator: IdGenerator.name // Note: doesn't apply in unit tests
		description type: 'text'
		previewConfigJson type: 'text'
		imageUrl length: 2048

		ownerAddress index: "owner_address_idx"
		beneficiaryAddress index: "beneficiary_address_idx"
	}
}
