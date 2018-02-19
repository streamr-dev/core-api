package com.unifina.controller.api

import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import com.unifina.service.ApiService
import com.unifina.service.ProductService
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class ProductStreamsApiController {

	ApiService apiService
	ProductService productService

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def index(String productId) {
		// TODO: should be done by StreamApiController#index? But different permission requirements. The aforementioned
		// requires READ permission on Stream just to show information.
		Product product = productService.findById(productId, loggedInUser(), Permission.Operation.READ)
		render(product.streams*.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def update(String productId, String id) {
		Product product = productService.findById(productId, loggedInUser(), Permission.Operation.WRITE)
		Stream stream = apiService.getByIdAndThrowIfNotFound(Stream, id)
		productService.addStreamToProduct(product, stream, loggedInUser())
		render(status: 204)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def delete(String productId, String id) {
		Product product = productService.findById(productId, loggedInUser(), Permission.Operation.WRITE)
		Stream stream = apiService.getByIdAndThrowIfNotFound(Stream, id)
		productService.removeStreamFromProduct(product, stream)
		render(status: 204)
	}

	SecUser loggedInUser() {
		request.apiUser
	}
}
