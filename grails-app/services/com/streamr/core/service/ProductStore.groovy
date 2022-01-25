package com.streamr.core.service

import com.streamr.core.domain.Product

class ProductStore {

	List<Product> findProductsByStream(String s) {
		return Product.createCriteria().list {
			streams {
				idEq(s)
			}
		}
	}
}
