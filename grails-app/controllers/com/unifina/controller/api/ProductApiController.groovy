package com.unifina.controller.api

import com.unifina.api.*
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import com.unifina.service.*
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.web.multipart.MultipartFile

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class ProductApiController {

	static allowedMethods = [
		setDeploying: "POST",
		setDeployed: "POST",
		setUndeploying: "POST",
		setUndeployed: "POST",
		setPricing: "POST",
		uploadImage: "POST",
		deployFree: "POST",
		undeployFree: "POST"
	]

	ApiService apiService
	FreeProductService freeProductService
	ProductService productService
	ProductImageService productImageService

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def related() {
		Product product = productService.findById((String) params.id, loggedInUser(), Permission.Operation.READ)
		int max = Math.min(params.int('max', 3), 10)
		def related = productService.relatedProducts(product, max, loggedInUser())
		render(related*.toSummaryMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def index(ProductListParams listParams) {
		def products = productService.list(listParams, loggedInUser())
		apiService.addLinkHintToHeader(listParams, products.size(), params, response)
		render(products*.toSummaryMap() as JSON)
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
	def setDeploying(String id) {
		Product product = productService.findById(id, loggedInUser(), Permission.Operation.WRITE)
		productService.transitionToDeploying(product)
		render(product.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def setPricing(String id, SetPricingCommand command) {
		Product product = apiService.getByIdAndThrowIfNotFound(Product, id)
		productService.updatePricing(product, command, loggedInUser())
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
	def deployFree(String id) {
		Product product = productService.findById(id, loggedInUser(), Permission.Operation.SHARE)
		freeProductService.deployFreeProduct(product)
		render(product.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def undeployFree(String id) {
		Product product = productService.findById(id, loggedInUser(), Permission.Operation.SHARE)
		freeProductService.undeployFreeProduct(product)
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
	def setUndeployed(String id, ProductUndeployedCommand command) {
		Product product = apiService.getByIdAndThrowIfNotFound(Product, id)
		productService.markAsUndeployed(product, command, loggedInUser())
		render(product.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def uploadImage(String id) {
		Product product = productService.findById(id, loggedInUser(), Permission.Operation.WRITE)
		MultipartFile file = getUploadedFile()
		productImageService.replaceImage(product, file.bytes, file.getOriginalFilename())
		render(product.toMap() as JSON)
	}

	MultipartFile getUploadedFile() {
		MultipartFile file = request.getFile("file")
		if (file == null) {
			throw new ApiException(400, "PARAMETER_MISSING", "Parameter 'file' missing")
		}
		return file
	}

	SecUser loggedInUser() {
		request.apiUser
	}
}
