package com.unifina.service

import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.api.ProductListParams
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class ProductService {
	ApiService apiService

	List<Product> list(ProductListParams listParams, SecUser currentUser) {
		apiService.list(Product, listParams, currentUser)
	}

	Product findById(String id, SecUser currentUser) throws NotFoundException, NotPermittedException {
		apiService.authorizedGetById(Product, id, currentUser, Permission.Operation.READ)
	}
}
