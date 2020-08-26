package com.unifina.api

import com.unifina.domain.Stream
import com.unifina.domain.Category
import com.unifina.domain.Contact
import com.unifina.domain.Product
import com.unifina.domain.TermsOfUse
import grails.validation.Validateable

@Validateable
class CreateProductCommand {
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
