package com.unifina.domain.marketplace

import com.unifina.domain.data.Stream
import com.unifina.domain.security.Permission
import com.unifina.utils.IdGenerator
import grails.compiler.GrailsCompileStatic
import groovy.transform.CompileStatic

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

	static hasMany = [
		permissions: Permission,
		streams: Stream
	]

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
		name(blank: false)
		description(blank: false)
		imageUrl(nullable: true)
		tx(nullable: true, validator: isEthereumTransaction)
		streams(maxSize: 1000)
		previewStream(nullable: true, validator: { Stream s, p -> s == null || s in p.streams }) // TODO: unit-test validation
		previewConfigJson(nullable: true)
		ownerAddress(validator: isEthereumAddress)
		beneficiaryAddress(validator: isEthereumAddress)
		pricePerSecond(min: 0L)
		minimumSubscriptionInSeconds(min: 0L)
	}

	static mapping = {
		id generator: IdGenerator.name // Note: doesn't apply in unit tests
		description type: 'text'
		previewConfigJson type: 'text'
		imageUrl length: 2048

		ownerAddress index: "owner_address_idx"
		beneficiaryAddress index: "beneficiary_address_idx"
	}

	@GrailsCompileStatic
	Map toMap() {
		[
		    id: id,
			name: name,
			description: description,
			imageUrl: imageUrl,
			category: category.id,
			streams: streams*.id,
			state: state.toString(),
			tx: tx,
			previewStream: previewStream,
			previewConfigJson: previewConfigJson,
			created: dateCreated,
			updated: lastUpdated,
			ownerAddress: ownerAddress,
			beneficiaryAddress: beneficiaryAddress,
			pricePerSecond: pricePerSecond,
			priceCurrency: priceCurrency.toString(),
			minimumSubscriptionInSeconds: minimumSubscriptionInSeconds
		]
	}

	static isEthereumAddress = { String value, object ->
		value ==~ /^0x[a-fA-F0-9]{40}$/ ?: "validation.isEthereumAddress"
	}

	static isEthereumTransaction = { String value, object ->
		value == null ?: value ==~ /^0x[a-fA-F0-9]{64}$/ ?: "validation.isEthereumTransaction"
	}
}
