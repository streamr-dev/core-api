package com.unifina.controller
import com.unifina.domain.Permission
import com.unifina.domain.Product
import com.unifina.domain.User
import com.unifina.service.ProductService
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON

class ProductStreamsApiController {
	ProductService productService

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def index(String productId) {
		Product product = productService.findById(productId, loggedInUser(), Permission.Operation.PRODUCT_GET)
		render(product.streams as JSON)
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
