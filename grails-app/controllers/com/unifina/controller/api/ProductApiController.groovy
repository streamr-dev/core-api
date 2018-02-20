package com.unifina.controller.api

import com.unifina.api.CreateProductCommand
import com.unifina.api.ProductDeployedCommand
import com.unifina.api.ProductListParams
import com.unifina.api.SetDeployingCommand
import com.unifina.api.UpdateProductCommand
import com.unifina.api.ValidationException
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
class ProductApiController {

	static allowedMethods = [
		setDeploying: "POST",
		setDeployed: "POST",
		setUndeploying: "POST",
		setUndeployed: "POST"
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
		Product product = productService.findById(id, loggedInUser(), Permission.Operation.READ)
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
	def update(String id, UpdateProductCommand command) {
		Product product = productService.update(id, command, loggedInUser())
		render(product.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def setDeploying(String id, SetDeployingCommand command) {
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}
		Product product = productService.findById(id, loggedInUser(), Permission.Operation.WRITE)
		productService.transitionToDeploying(product, command.tx)
		render(product.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def setDeployed(String id, ProductDeployedCommand command) {
		Product product = apiService.getByIdAndThrowIfNotFound(Product, id)
		productService.markAsDeployed(product, command, loggedInUser())
		render(product.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def setUndeploying(String id) {
		Product product = productService.findById(id, loggedInUser(), Permission.Operation.WRITE)
		productService.transitionToUndeploying(product)
		render(product.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def setUndeployed(String id) {
		Product product = apiService.getByIdAndThrowIfNotFound(Product, id)
		productService.markAsUndeployed(product, loggedInUser())
		render(status: 204)
	}

	SecUser loggedInUser() {
		request.apiUser
	}
}
