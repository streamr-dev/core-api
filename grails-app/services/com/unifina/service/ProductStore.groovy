package com.unifina.service

import com.unifina.domain.Product

class ProductStore {
	List<Product> findProductsByStream(String s) {
		return Product.createCriteria().list {
			streams {
				idEq(s)
			}
		}
	}
}
