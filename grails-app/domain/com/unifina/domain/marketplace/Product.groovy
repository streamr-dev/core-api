package com.unifina.domain.marketplace

import com.unifina.domain.data.Stream
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.utils.EmailValidator
import com.unifina.utils.HexIdGenerator
import grails.compiler.GrailsCompileStatic
import groovy.json.JsonSlurper

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

	static class Contact {
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

		public Contact() {}
		public Contact(Product product) {}

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

	static class TermsOfUse {
		Boolean redistribution = true
		Boolean commercialUse = true
		Boolean reselling = true
		Boolean storage = true
		String termsUrl
		String termsName

		public TermsOfUse() {}
		public TermsOfUse(Product product) {}

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

	// Product's contact details.
	Contact contact
	// Product's legal terms of use.
	TermsOfUse termsOfUse

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
