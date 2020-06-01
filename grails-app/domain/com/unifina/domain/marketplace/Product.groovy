package com.unifina.domain.marketplace

import com.unifina.domain.data.Stream
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.utils.HexIdGenerator
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import groovy.json.JsonSlurper

@Entity
class Product {
	public final static String DEFAULT_NAME = "Untitled Product"
	String id
	String name = DEFAULT_NAME
	String description
	String imageUrl
	String thumbnailUrl

	// Type of the product is either normal or data union.
	Type type = Type.NORMAL
	Category category
	State state = State.NOT_DEPLOYED
	Stream previewStream
	String previewConfigJson
	String pendingChanges

	Date dateCreated
	Date lastUpdated
	Integer score = 0 // set manually; used as default ordering for lists of Products (descending)
	SecUser owner // set to product creator when product is created.

	// Product's contact details.
	Contact contact = new Contact()
	// Product's legal terms of use.
	TermsOfUse termsOfUse = new TermsOfUse()

	static embedded = [
		'contact',
		'termsOfUse',
	]

	// The below fields exist in the domain object for speed & query support, but the ground truth is in the smart contract.
	String ownerAddress
	String beneficiaryAddress
	Long pricePerSecond = 0
	Currency priceCurrency = Currency.DATA
	Long minimumSubscriptionInSeconds = 0
	Long blockNumber = 0
	Long blockIndex = 0

	static hasMany = [
		permissions: Permission,
		streams: Stream
	]

	enum Type {
		NORMAL,
		DATAUNION
	}

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
		description(nullable: true)
		imageUrl(nullable: true)
		thumbnailUrl(nullable: true)
		category(nullable: true)
		type(nullable: false)
		previewStream(nullable: true, validator: { Stream s, p -> s == null || s in p.streams })
		previewConfigJson(nullable: true)
		pendingChanges(nullable: true)
		ownerAddress(nullable: true, validator: isEthereumAddressOrIsNull)
		beneficiaryAddress(nullable: true, validator: isEthereumAddressOrIsNull)
		pricePerSecond(min: 0L)
		minimumSubscriptionInSeconds(min: 0L)
		blockNumber(min: 0L)
		blockIndex(min: 0L)
		owner(nullable: false)
		contact(nullable: true)
		termsOfUse(nullable: true)
	}

	static mapping = {
		id generator: HexIdGenerator.name // Note: doesn't apply in unit tests
		description type: 'text'
		type enumType: "identity", defaultValue: Type.NORMAL, index: 'type_idx'
		previewConfigJson type: 'text'
		pendingChanges type: 'text'
		imageUrl length: 2048
		score index: "score_idx"

		ownerAddress index: "owner_address_idx"
		beneficiaryAddress index: "beneficiary_address_idx"
		owner(fetch: 'join')
	}

	@GrailsCompileStatic
	Map toMap(boolean isOwner = false) {
		def map = [
		    id: id,
			type: type.toString(),
			name: name,
			description: description,
			imageUrl: imageUrl,
			thumbnailUrl: thumbnailUrl,
			category: category?.id,
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
			owner: owner.name,
			contact: contact?.toMap(),
			termsOfUse: termsOfUse?.toMap(),
		]
		if (isOwner && pendingChanges != null) {
			JsonSlurper slurper = new JsonSlurper()
			map.put("pendingChanges", (HashMap<String, Serializable>) slurper.parseText(pendingChanges))
		}
		return map
	}

	@GrailsCompileStatic
	Map toSummaryMap() {
		[
			id: id,
			type: type.toString(),
			name: name,
			description: description,
			imageUrl: imageUrl,
			thumbnailUrl: thumbnailUrl,
			category: category?.id,
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

	boolean isFree() {
		return pricePerSecond == 0
	}

	static isEthereumAddressOrIsNull = { String value ->
		value == null || Product.isEthereumAddress(value)
	}

	static isEthereumAddress = { String value ->
		value ==~ /^0x[a-fA-F0-9]{40}$/ ?: "validation.isEthereumAddress"
	}
}
