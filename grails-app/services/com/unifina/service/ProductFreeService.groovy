package com.unifina.service

import com.unifina.domain.Permission
import com.unifina.domain.Product
import grails.compiler.GrailsCompileStatic

/**
 * TODO: merge ProductService and ProductFreeService
 */
@GrailsCompileStatic
class ProductFreeService {
	PermissionService permissionService

	void deployFreeProduct(Product product) {
		verifyThatProductIsFree(product)
		if (product.state == Product.State.DEPLOYED) { // TODO: state transition mismatch with paid products
			throw new InvalidStateTransitionException(product.state, Product.State.DEPLOYED)
		}

		// TODO: these 3 statements also in ProductService#markAsDeployed
		product.state = Product.State.DEPLOYED
		product.save(failOnError: true)
		permissionService.systemGrantAnonymousAccess(product, Permission.Operation.PRODUCT_GET)

		product.streams.each {
			permissionService.systemGrantAnonymousAccess(it, Permission.Operation.STREAM_GET)
			permissionService.systemGrantAnonymousAccess(it, Permission.Operation.STREAM_SUBSCRIBE)
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
		permissionService.systemRevokeAnonymousAccess(product, Permission.Operation.PRODUCT_GET)

		product.streams.each {
			permissionService.systemRevokeAnonymousAccess(it, Permission.Operation.STREAM_GET)
			permissionService.systemRevokeAnonymousAccess(it, Permission.Operation.STREAM_SUBSCRIBE)
		}
	}

	static void verifyThatProductIsFree(Product product) {
		if (!product.isFree()) {
			throw new ProductNotFreeException(product)
		}
	}
}
