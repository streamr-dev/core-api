package com.streamr.core.controller

import com.streamr.core.domain.Permission
import com.streamr.core.domain.Product
import com.streamr.core.domain.User
import com.streamr.core.service.ProductService
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON

class ProductStreamsApiController {
	ProductService productService

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def index(String productId) {
		Product product = productService.findById(productId, loggedInUser(), Permission.Operation.PRODUCT_GET)
		render(product.streams*.toString() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def update(String productId, String id) {
		final String streamId = id
		Product product = productService.findById(productId, loggedInUser(), Permission.Operation.PRODUCT_EDIT)
		if (!product.streams.contains(streamId)) {
			productService.addStreamToProduct(product, streamId, loggedInUser())
		}
		render(status: 204)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def delete(String productId, String id) {
		final String streamId = id
		Product product = productService.findById(productId, loggedInUser(), Permission.Operation.PRODUCT_DELETE)
		if (product.streams.contains(streamId)) {
			productService.removeStreamFromProduct(product, streamId)
		}
		render(status: 204)
	}

	User loggedInUser() {
		request.apiUser
	}
}
