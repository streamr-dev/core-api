package com.unifina.controller.api

import com.unifina.api.CreateProductCommand
import com.unifina.api.ProductListParams
import com.unifina.api.SetDeployingCommand
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

	static allowedMethods = [
		setDeploying: "POST",
		setDeleting: "POST"
	]

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

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def save(CreateProductCommand command) {
		Product product = productService.create(command, loggedInUser())
		render(product.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def delete(String id) {
		Product product = apiService.getByIdAndThrowIfNotFound(Product, id)
		productService.delete(product, loggedInUser())
		render(status: 204)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def setDeploying(String id, SetDeployingCommand command) {
		Product product = productService.findById(id, loggedInUser())
		productService.transitionToDeploying(product, command.tx)
		render(product.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def setDeleting(String id) {
		Product product = productService.findById(id, loggedInUser())
		productService.transitionToDeleting(product)
		render(product.toMap() as JSON)
	}

	SecUser loggedInUser() {
		request.apiUser
	}
}
