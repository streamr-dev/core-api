package com.unifina.service

import com.unifina.domain.Product
import com.unifina.domain.Stream

class ProductStore {
	List<Product> findProductsByStream(Stream s) {
		return Product.createCriteria().list {
			streams {
				idEq(s.id)
			}
		}
	}
}
