package com.unifina.domain.marketplace

import com.unifina.domain.data.Stream
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.utils.HexIdGenerator
import grails.compiler.GrailsCompileStatic

class Product {
	String id
	String name
	String description
	String imageUrl
	String thumbnailUrl

	Category category
	State state = State.NOT_DEPLOYED
	Stream previewStream
	String previewConfigJson

	Date dateCreated
	Date lastUpdated
	Integer score = 0 // set manually; used as default ordering for lists of Products (descending)
	SecUser owner // set to product creator when product is created.

	// The below fields exist in the domain object for speed & query support, but the ground truth is in the smart contract.
	String ownerAddress
	String beneficiaryAddress
	Long pricePerSecond
	Currency priceCurrency = Currency.DATA
	Long minimumSubscriptionInSeconds = 0
	Long blockNumber = 0
	Long blockIndex = 0

	static hasMany = [
		permissions: Permission,
		streams: Stream
	]

	enum State {
		NOT_DEPLOYED,
		DEPLOYING,
		DEPLOYED,
		UNDEPLOYING
	}

	enum Currency {
		DATA,
		USD
	}

	static constraints = {
		name(blank: false)
		description(blank: false)
		imageUrl(nullable: true)
		thumbnailUrl(nullable: true)
		streams(maxSize: 1000)
		previewStream(nullable: true, validator: { Stream s, p -> s == null || s in p.streams })
		previewConfigJson(nullable: true)
		ownerAddress(nullable: true, validator: isEthereumAddressOrIsNull)
		beneficiaryAddress(nullable: true, validator: isEthereumAddressOrIsNull)
		pricePerSecond(min: 0L, validator: { Long price, p ->
			price == 0 ?
				p.ownerAddress == null && p.beneficiaryAddress == null :
				p.ownerAddress != null && p.beneficiaryAddress != null
		})
		minimumSubscriptionInSeconds(min: 0L)
		blockNumber(min: 0L)
		blockIndex(min: 0L)
		owner(nullable: false)
	}

	static mapping = {
		id generator: HexIdGenerator.name // Note: doesn't apply in unit tests
		description type: 'text'
		previewConfigJson type: 'text'
		imageUrl length: 2048
		score index: "score_idx"

		ownerAddress index: "owner_address_idx"
		beneficiaryAddress index: "beneficiary_address_idx"
		owner(fetch: 'join')
	}

	@GrailsCompileStatic
	Map toMap() {
		[
		    id: id,
			name: name,
			description: description,
			imageUrl: imageUrl,
			thumbnailUrl: thumbnailUrl,
			category: category.id,
			streams: streams*.id,
			state: state.toString(),
			previewStream: previewStream?.id,
			previewConfigJson: previewConfigJson,
			created: dateCreated,
			updated: lastUpdated,
			ownerAddress: ownerAddress,
			beneficiaryAddress: beneficiaryAddress,
			pricePerSecond: pricePerSecond.toString(),
			isFree: pricePerSecond == 0L,
			priceCurrency: priceCurrency.toString(),
			minimumSubscriptionInSeconds: minimumSubscriptionInSeconds,
			owner: owner.name
		]
	}

	@GrailsCompileStatic
	Map toSummaryMap() {
		[
			id: id,
			name: name,
			description: description,
			imageUrl: imageUrl,
			thumbnailUrl: thumbnailUrl,
			category: category.id,
			streams: [],
			state: state.toString(),
			previewStream: previewStream?.id,
			previewConfigJson: previewConfigJson,
			created: dateCreated,
			updated: lastUpdated,
			ownerAddress: ownerAddress,
			beneficiaryAddress: beneficiaryAddress,
			pricePerSecond: pricePerSecond.toString(),
			isFree: pricePerSecond == 0L,
			priceCurrency: priceCurrency.toString(),
			minimumSubscriptionInSeconds: minimumSubscriptionInSeconds,
			owner: owner.name
		]
	}

	static isEthereumAddressOrIsNull = { String value ->
		value == null || Product.isEthereumAddress(value)
	}

	static isEthereumAddress = { String value ->
		value ==~ /^0x[a-fA-F0-9]{40}$/ ?: "validation.isEthereumAddress"
	}
}
