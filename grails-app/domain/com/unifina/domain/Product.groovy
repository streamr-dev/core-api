package com.unifina.domain

import com.unifina.utils.HexIdGenerator
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import grails.validation.Validateable
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
	User owner // set to product creator when product is created.

	// Product's contact details.
	Contact contact = new Contact()
	// Product's legal terms of use.
	TermsOfUse termsOfUse = new TermsOfUse()

	static embedded = [
		'contact',
		'termsOfUse',
	]

	// Chain where product belogs to
	Chain chain = Chain.ETHEREUM

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
		streams: Stream,
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

	// Product belongs to a chain
	enum Chain {
		ETHEREUM,
		XDAI,
		POLYGON,
		BSC,
		AVALANCHE
	}

	static constraints = {
		name(blank: false)
		description(nullable: true)
		imageUrl(nullable: true)
		thumbnailUrl(nullable: true)
		category(nullable: true)
		state(enumType: "string")
		type(nullable: false, enumType: "ordinal")
		previewStream(nullable: true, validator: { Stream s, p -> s == null || s in p.streams })
		previewConfigJson(nullable: true)
		pendingChanges(nullable: true)
		ownerAddress(nullable: true, validator: isEthereumAddressOrIsNull)
		beneficiaryAddress(nullable: true, validator: isEthereumAddressOrIsNull)
		pricePerSecond(min: 0L)
		priceCurrency(enumType: "string")
		minimumSubscriptionInSeconds(min: 0L)
		blockNumber(min: 0L)
		blockIndex(min: 0L)
		owner(nullable: false)
		contact(nullable: true)
		termsOfUse(nullable: true)
		chain(enumType: "string", nullable: false, inList: Chain.values() as List)
	}

	static mapping = {
		id generator: HexIdGenerator.name // Note: doesn't apply in unit tests
		description type: 'text'
		type enumType: "ordinal", defaultValue: Type.NORMAL.ordinal(), index: 'type_idx'
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
			isFree: this.isFree(),
			priceCurrency: priceCurrency.toString(),
			minimumSubscriptionInSeconds: minimumSubscriptionInSeconds,
			owner: owner.name,
			contact: contact?.toMap(),
			termsOfUse: termsOfUse?.toMap(),
			chain: chain.toString(),
		]
		if (isOwner && pendingChanges != null) {
			JsonSlurper slurper = new JsonSlurper()
			map.put("pendingChanges", (HashMap<String, Serializable>) slurper.parseText(pendingChanges))
		}
		return map
	}

	@GrailsCompileStatic
	Map toSummaryMap() {
		def map = [
			id: id,
			type: type.toString(),
			name: name,
			description: description,
			imageUrl: imageUrl,
			thumbnailUrl: thumbnailUrl,
			category: category?.id,
			state: state.toString(),
			previewStream: previewStream?.id,
			previewConfigJson: previewConfigJson,
			created: dateCreated,
			updated: lastUpdated,
			ownerAddress: ownerAddress,
			beneficiaryAddress: beneficiaryAddress,
			pricePerSecond: pricePerSecond.toString(),
			isFree: this.isFree(),
			priceCurrency: priceCurrency.toString(),
			minimumSubscriptionInSeconds: minimumSubscriptionInSeconds,
			owner: owner.name,
			chain: chain.toString(),
		]
		return map;
	}

	boolean isFree() {
		return pricePerSecond == 0L
	}

	static isEthereumAddressOrIsNull = { String value ->
		value == null || Product.isEthereumAddress(value)
	}

	static isEthereumAddress = { String value ->
		value ==~ /^0x[a-fA-F0-9]{40}$/ ?: "validation.isEthereumAddress"
	}
}

@GrailsCompileStatic
@Validateable
class Contact implements Serializable {
	// Contact's email address.
	String email
	// Contact's URL address.
	String url
	// Social media link 1
	String social1
	// Social media link 2
	String social2
	// Social media link 3
	String social3
	// Social media link 4
	String social4

	static constraints = {
		email(nullable: true, validator: EmailValidator.validateNullEmail, maxSize: 255)
		url(nullable: true, url: true, maxSize: 2048)
		social1(nullable: true, url: true, maxSize: 2048)
		social2(nullable: true, url: true, maxSize: 2048)
		social3(nullable: true, url: true, maxSize: 2048)
		social4(nullable: true, url: true, maxSize: 2048)
	}

	Map toMap() {
		return [
			email: email,
			url: url,
			social1: social1,
			social2: social2,
			social3: social3,
			social4: social4,
		]
	}
}

@GrailsCompileStatic
@Validateable
class TermsOfUse implements Serializable {
	Boolean redistribution = true
	Boolean commercialUse = true
	Boolean reselling = true
	Boolean storage = true
	String termsUrl
	String termsName

	static constraints = {
		redistribution(nullable: false)
		commercialUse(nullable: false)
		reselling(nullable: false)
		storage(nullable: false)
		termsUrl(nullable: true, url: true, maxSize: 2048)
		termsName(nullable: true, maxSize: 100)
	}

	Map toMap() {
		return [
			redistribution: redistribution,
			commercialUse: commercialUse,
			reselling: reselling,
			storage: storage,
			termsUrl: termsUrl,
			termsName: termsName,
		]
	}
}
