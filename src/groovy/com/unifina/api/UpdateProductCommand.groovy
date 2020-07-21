package com.unifina.api

import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Category
import com.unifina.domain.marketplace.Contact
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Permission
import com.unifina.domain.marketplace.TermsOfUse
import com.unifina.domain.security.SecUser
import com.unifina.service.PermissionService
import grails.compiler.GrailsCompileStatic
import grails.validation.Validateable
import groovy.json.JsonBuilder

@Validateable
class UpdateProductCommand {
	String name
	String description

	Set<Stream> streams = []

	Category category
	Stream previewStream
	String previewConfigJson
	Map<String, Object> pendingChanges
	Contact contact = new Contact()
	TermsOfUse termsOfUse = new TermsOfUse()

	// Below are used only when updating NOT_DEPLOYED product
	String ownerAddress
	String beneficiaryAddress
	Long pricePerSecond
	Product.Currency priceCurrency
	Long minimumSubscriptionInSeconds

	public static final List<String> offChainFields = [
		"name",
		"description",
		"streams",
		"category",
		"previewStream",
		"previewConfigJson",
		"pendingChanges",
		"contact",
		"termsOfUse",
	]

	public static final List<String> onChainFields = [
		"ownerAddress",
		"beneficiaryAddress",
		"pricePerSecond",
		"priceCurrency",
		"minimumSubscriptionInSeconds"
	]

	static constraints = {
		importFrom(Product)
		// List all onChainFields as nullable
		ownerAddress(nullable: true, validator: Product.isEthereumAddressOrIsNull)
		beneficiaryAddress(nullable: true, validator: Product.isEthereumAddressOrIsNull)
		pricePerSecond(nullable: true)
		priceCurrency(nullable: true)
		minimumSubscriptionInSeconds(nullable: true)
	}

	@GrailsCompileStatic
	void updateProduct(Product product, SecUser user, PermissionService permissionService) {
		// Always update off-chain fields if given
		offChainFields.forEach { String fieldName ->
			product[fieldName] = this[fieldName]
		}

		// Prevent deployed products from changing from free to paid
		if (product.pricePerSecond == 0 && product.state == Product.State.DEPLOYED && this.pricePerSecond > 0) {
			throw new FieldCannotBeUpdatedException("Published products can't be changed from free to paid.")
		}

		// Prevent the user from changing on-chain fields of paid deployed products.
		// They must be updated on the smart contract and updated by the watcher.
		List changedOnChainFields = onChainFields.findAll {this[it] != null && this[it] != product[it]}
		if (product.pricePerSecond > 0 && product.state == Product.State.DEPLOYED && !changedOnChainFields.isEmpty()) {
			throw new FieldCannotBeUpdatedException("For published paid products, the following fields can only be updated on the smart contract: ${onChainFields}. You tried to change fields: ${changedOnChainFields}")
		}

		// Otherwise all good. Update on-chain fields only if given (they can be omitted).
		onChainFields.forEach { String fieldName ->
			if (this[fieldName] != null) {
				product[fieldName] = this[fieldName]
			}
		}

		if (pendingChanges != null) {
			if (!permissionService.check(user, product, Permission.Operation.PRODUCT_SHARE)) {
				throw new FieldCannotBeUpdatedException("User doesn't have permission to share product.")
			} else {
				product.pendingChanges = new JsonBuilder(pendingChanges).toString()
			}
		}
	}
}
