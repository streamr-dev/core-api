package com.unifina.controller.api

import com.unifina.api.ProductListParams
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.SecUser
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import com.unifina.service.ApiService
import com.unifina.service.ProductService
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class ProductApiController {
	ApiService apiService
	ProductService productService

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def index(ProductListParams listParams) {
		def products = productService.list(listParams, loggedInUser())
		apiService.addLinkHintToHeader(listParams, products.size(), params, response)
		render(products*.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def show(String id) {
		Product product = productService.findById(id, loggedInUser())
		render(product.toMap() as JSON)
	}

	SecUser loggedInUser() {
		request.apiUser
	}
}
