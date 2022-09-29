package com.streamr.core.service

import com.streamr.core.domain.Category
import com.streamr.core.domain.Permission
import com.streamr.core.domain.Product
import com.streamr.core.domain.User
import grails.validation.Validateable

@Validateable
class ProductListParams extends ListParams {
	Set<Category> categories
	Set<Product.State> states
	Boolean free
	User productOwner
	String type
	String beneficiaryAddress

	ProductListParams() {
		super()
		operation = Permission.Operation.PRODUCT_GET.toString()
	}

	static constraints = {
		categories(nullable: true, minSize: 1)
		states(nullable: true, minSize: 1)
		free(nullable: true)
		productOwner(nullable: true)
		type(nullable: true)
		beneficiaryAddress(nullable: true)
	}

	@Override
	protected List<String> getSearchFields() {
		return ["name", "description"]
	}

	@Override
	protected Closure additionalCriteria() {
		return {
			if (categories) {
				'in'("category", categories)
			}
			if (states) {
				'in'("state", states)
			}
			if (free != null && free) {
				eq("pricePerSecond", "0")
			}
			if (free != null && !free) {
				ne("pricePerSecond", "0")
			}
			if (productOwner != null) {
				eq("owner", productOwner)
			}
			if (type != null) {
				try {
					Product.Type value = Product.Type.valueOf(type.toUpperCase())
					eq("type", value)
				} catch (final IllegalArgumentException ignored) {
				}
			}
			if (beneficiaryAddress != null) {
				eq("beneficiaryAddress", beneficiaryAddress)
			}
		}
	}

	@Override
	Map<String, Object> toMap() {
		return super.toMap() + [
			categories: categories*.id,
			states    : states*.id,
			free      : free,
			owner     : productOwner,
			type      : type,
			beneficiaryAddress: beneficiaryAddress,
		] as Map<String, Object>
	}
}
