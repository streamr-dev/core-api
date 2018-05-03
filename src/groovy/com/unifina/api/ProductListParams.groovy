package com.unifina.api

import com.unifina.domain.marketplace.Category
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.SecUser
import grails.validation.Validateable

@Validateable
class ProductListParams extends ListParams {
	Set<Category> categories
	Set<Product.State> states
	Long minPrice
	Long maxPrice
	SecUser productOwner

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
