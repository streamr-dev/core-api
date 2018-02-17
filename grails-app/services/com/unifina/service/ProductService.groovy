package com.unifina.service

import com.unifina.api.CreateProductCommand
import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.api.ProductListParams
import com.unifina.api.ValidationException
import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class ProductService {
	ApiService apiService
	PermissionService permissionService

	List<Product> list(ProductListParams listParams, SecUser currentUser) {
		apiService.list(Product, listParams, currentUser)
	}

	Product findById(String id, SecUser currentUser) throws NotFoundException, NotPermittedException {
		apiService.authorizedGetById(Product, id, currentUser, Permission.Operation.READ)
	}

	Product create(CreateProductCommand command, SecUser currentUser) throws ValidationException {
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}

		command.streams.each {
			permissionService.verifyShare(currentUser, it)
		}

		Product product = new Product(command.properties)
		product.save(failOnError: true)
		permissionService.systemGrantAll(currentUser, product)
		return product
	}
}
