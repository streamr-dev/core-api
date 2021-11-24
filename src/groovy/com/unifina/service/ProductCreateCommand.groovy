package com.unifina.service

import com.unifina.domain.*
import grails.validation.Validateable

@Validateable
class ProductCreateCommand {
	Product.Type type = Product.Type.NORMAL

	String name = Product.DEFAULT_NAME
	String description

	Category category
	Stream previewStream
	String previewConfigJson

	Set<Stream> streams = []

	String ownerAddress
	String beneficiaryAddress
	Long pricePerSecond = 0
	Product.Currency priceCurrency = Product.Currency.DATA
	Long minimumSubscriptionInSeconds = 0
	Contact contact = new Contact()
	TermsOfUse termsOfUse = new TermsOfUse()

	static constraints = {
		importFrom(Product)
	}
}
