package com.unifina.controller

import com.unifina.domain.Permission
import com.unifina.domain.Product
import com.unifina.domain.Stream
import com.unifina.domain.User
import com.unifina.service.ApiService
import com.unifina.service.ProductService
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON

class ProductStreamsApiController {

	ApiService apiService
	ProductService productService

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def index(String productId) {
		// TODO: should be done by StreamApiController#index? But different permission requirements. The aforementioned
		// requires stream_get permission on Stream just to show information.
		Product product = productService.findById(productId, loggedInUser(), Permission.Operation.PRODUCT_GET)
		render(product.streams*.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def update(String productId, String id) {
		Product product = productService.findById(productId, loggedInUser(), Permission.Operation.PRODUCT_EDIT)
		Stream stream = apiService.getByIdAndThrowIfNotFound(Stream, id)
		productService.addStreamToProduct(product, stream, loggedInUser())
		render(status: 204)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def delete(String productId, String id) {
		Product product = productService.findById(productId, loggedInUser(), Permission.Operation.PRODUCT_DELETE)
		Stream stream = apiService.getByIdAndThrowIfNotFound(Stream, id)
		productService.removeStreamFromProduct(product, stream)
		render(status: 204)
	}

    User loggedInUser() {
		request.apiUser
	}
}
