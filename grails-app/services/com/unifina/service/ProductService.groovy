package com.unifina.service

import com.unifina.api.*
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

	Product findById(String id, SecUser currentUser, Permission.Operation op)
			throws NotFoundException, NotPermittedException {
		apiService.authorizedGetById(Product, id, currentUser, op)
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

	void transitionToDeploying(Product product, String tx) {
		if (product.state == Product.State.NEW) {
			product.tx = tx
			product.state = Product.State.DEPLOYING
			product.save(failOnError: true)
		} else {
			throw new InvalidStateTransitionException(product.state, Product.State.DEPLOYING)
		}
	}

	void transitionToDeleting(Product product) {
		if (product.state == Product.State.DEPLOYED) {
			product.state = Product.State.DELETING
			product.save(failOnError: true)
		} else {
			throw new InvalidStateTransitionException(product.state, Product.State.DELETING)
		}
	}

	void delete(Product product, SecUser currentUser) throws NotPermittedException {
		if (!(product.state in [Product.State.DEPLOYED, Product.State.DELETING, Product.State.DELETED])) {
			throw new InvalidStateTransitionException(product.state, Product.State.DELETED)
		}
		if (!currentUser.isDevOps()) {
			throw new NotPermittedException("DevOps role required")
		}
		permissionService.systemRevokeAnonymousAccess(product)
		product.state = Product.State.DELETED
		product.save(failOnError: true)
	}
}
