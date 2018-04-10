package com.unifina.service

import com.unifina.api.InvalidStateTransitionException
import com.unifina.api.ProductNotFreeException
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Permission


/**
 * TODO: merge ProductService and FreeProductService
 */
class FreeProductService {
	PermissionService permissionService

	void deployFreeProduct(Product product) {
		if (product.pricePerSecond != 0) {
			throw new ProductNotFreeException(product)
		} else if (product.state == Product.State.DEPLOYED) { // TODO: state transition mismatch with paid products
			throw new InvalidStateTransitionException(product.state, Product.State.DEPLOYED)
		}

		// TODO: these 3 statements also in ProductService#markAsDeployed
		product.state = Product.State.DEPLOYED
		product.save(failOnError: true)
		permissionService.systemGrantAnonymousAccess(product)

		product.streams.each {
			permissionService.systemGrantAnonymousAccess(it, Permission.Operation.READ)
		}
	}
}
