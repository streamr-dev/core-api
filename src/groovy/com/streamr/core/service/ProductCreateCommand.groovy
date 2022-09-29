package com.streamr.core.service

import com.streamr.core.domain.Category
import com.streamr.core.domain.Contact
import com.streamr.core.domain.Product
import com.streamr.core.domain.TermsOfUse
import grails.validation.Validateable

@Validateable
class ProductCreateCommand {
	Product.Type type = Product.Type.NORMAL

	String name = Product.DEFAULT_NAME
	String description

	Category category
	String previewStreamId
	String previewConfigJson

	Set<String> streams = []

	String ownerAddress
	String beneficiaryAddress
	String pricePerSecond = "0"
	Product.Currency priceCurrency = Product.Currency.DATA
	Long minimumSubscriptionInSeconds = 0
	Contact contact = new Contact()
	TermsOfUse termsOfUse = new TermsOfUse()

	static constraints = {
		importFrom(Product)
	}
}
