package com.streamr.core.service

import com.streamr.core.domain.*
import grails.compiler.GrailsCompileStatic
import grails.validation.Validateable
import groovy.json.JsonBuilder

@Validateable
class ProductUpdateCommand {
	String name
	String description

	Set<String> streams = []

    Category category
	String previewStreamId
	String previewConfigJson
	Map<String, Object> pendingChanges
	Contact contact = new Contact()
	TermsOfUse termsOfUse = new TermsOfUse()
	Product.Chain chain

	// Below are used only when updating NOT_DEPLOYED product
	String ownerAddress
	String beneficiaryAddress
	String pricePerSecond
	Product.Currency priceCurrency
	Long minimumSubscriptionInSeconds

	public static final List<String> offChainFields = [
		"name",
		"description",
		"streams",
		"category",
		"previewStreamId",
		"previewConfigJson",
		"pendingChanges",
		"contact",
		"termsOfUse",
		"chain",
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
		chain(nullable: true)
		// List all onChainFields as nullable
		ownerAddress(nullable: true, validator: EthereumAddressValidator.isNullOrValid)
		beneficiaryAddress(nullable: true, validator: EthereumAddressValidator.isNullOrValid)
		pricePerSecond(nullable: true, validator: BigIntegerStringValidator.isNullOrNonNegative)
		priceCurrency(nullable: true)
		minimumSubscriptionInSeconds(nullable: true)
	}

	@GrailsCompileStatic
	void updateProduct(Product product, User user, PermissionService permissionService) {
		// Always update off-chain fields if given
		offChainFields.forEach { String fieldName ->
			if (fieldName != "chain") {
				product[fieldName] = this[fieldName]
			} else if (!product.writtenToChain && this[fieldName] != null) {
				product[fieldName] = this[fieldName]
			}
		}

		// Prevent deployed products from changing from free to paid
		if (product.isFree() && product.state == Product.State.DEPLOYED && !this.isFree()) {
			throw new FieldCannotBeUpdatedException("Published products can't be changed from free to paid.")
		}

		// Prevent the user from changing on-chain fields of paid deployed products.
		// They must be updated on the smart contract and updated by the watcher.
		List changedOnChainFields = onChainFields.findAll { this[it] != null && this[it] != product[it] }
		if (!product.isFree() && product.state == Product.State.DEPLOYED && !changedOnChainFields.isEmpty()) {
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

	boolean isFree() {
		Product p = new Product(pricePerSecond: pricePerSecond)
		return p.isFree()
	}
}
