package com.unifina.controller.api

import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import com.unifina.service.ProductService
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class ProductStreamsApiController {

	ProductService productService

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def index(String productId) {
		Product product = productService.findById(productId, loggedInUser(), Permission.Operation.READ)
		render(product.streams*.toMap() as JSON)
	}

	SecUser loggedInUser() {
		request.apiUser
	}
}
