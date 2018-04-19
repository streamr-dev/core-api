package com.unifina.service

import com.unifina.api.InvalidStateTransitionException
import com.unifina.api.ProductNotFreeException
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Permission
import grails.compiler.GrailsCompileStatic


/**
 * TODO: merge ProductService and FreeProductService
 */
@GrailsCompileStatic
class FreeProductService {
	PermissionService permissionService

	void deployFreeProduct(Product product) {
		verifyThatProductIsFree(product)
		if (product.state == Product.State.DEPLOYED) { // TODO: state transition mismatch with paid products
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

	void undeployFreeProduct(Product product) {
		verifyThatProductIsFree(product)
		if (product.state == Product.State.NOT_DEPLOYED) { // TODO: state transition mismatch with paid products
			throw new InvalidStateTransitionException(product.state, Product.State.NOT_DEPLOYED)
		}

		// TODO: these 3 statements also in ProductService#markAsDeployed
		product.state = Product.State.NOT_DEPLOYED
		product.save(failOnError: true)
		permissionService.systemRevokeAnonymousAccess(product)

		product.streams.each {
			permissionService.systemRevokeAnonymousAccess(it, Permission.Operation.READ)
		}
	}

	static void verifyThatProductIsFree(Product product) {
		if (product.pricePerSecond != 0) {
			throw new ProductNotFreeException(product)
		}
	}
}
