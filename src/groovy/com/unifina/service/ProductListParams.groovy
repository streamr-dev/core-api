package com.unifina.service

import com.unifina.domain.Category
import com.unifina.domain.Permission
import com.unifina.domain.Product
import com.unifina.domain.User
import com.unifina.service.ListParams
import grails.validation.Validateable

@Validateable
class ProductListParams extends ListParams {
	Set<Category> categories
	Set<Product.State> states
	Long minPrice
	Long maxPrice
	User productOwner

	ProductListParams() {
		super()
		operation = Permission.Operation.PRODUCT_GET
	}

	static constraints = {
		categories(nullable: true, minSize: 1)
		states(nullable: true, minSize: 1)
		minPrice(nullable: true, min: 0L)
		maxPrice(nullable: true)
		productOwner(nullable: true)
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
			if (minPrice != null) {
				ge("pricePerSecond", minPrice)
			}
			if (maxPrice != null) {
				le("pricePerSecond", maxPrice)
			}
			if (productOwner != null) {
				eq("owner", productOwner)
			}
		}
	}

	@Override
	Map toMap() {
		return super.toMap() + [
		    categories: categories*.id,
			states: states*.id,
			minPrice: minPrice,
			maxPrice: maxPrice,
			owner: productOwner
		]
	}
}
