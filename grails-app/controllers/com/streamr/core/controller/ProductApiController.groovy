package com.streamr.core.controller

import com.streamr.core.domain.Permission
import com.streamr.core.domain.Product
import com.streamr.core.domain.User
import com.streamr.core.service.*
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import org.apache.log4j.Logger
import org.springframework.web.multipart.MultipartFile

class ProductApiController {
	ApiService apiService
	ProductService productService
	ProductImageService productImageService
	PermissionService permissionService

	private static final Logger log = Logger.getLogger(ProductApiController)

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def related() {
		Product product = productService.findById((String) params.id, loggedInUser(), Permission.Operation.PRODUCT_GET)
		int max = Math.min(params.int('max', 3), 10)
		def related = productService.relatedProducts(product, max, loggedInUser())
		render(related*.toSummaryMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def index(ProductListParams listParams) {
		def products = productService.list(listParams, loggedInUser())
		PaginationUtils.setHint(response, listParams, products.size(), params)
		render(products*.toSummaryMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.NONE)
	def show(String id) {
		Product product = productService.findById(id, loggedInUser(), Permission.Operation.PRODUCT_GET)
		boolean isProductOwner = permissionService.check(loggedInUser(), product, Permission.Operation.PRODUCT_SHARE)
		render(product.toMap(isProductOwner) as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def save(ProductCreateCommand command) {
		Product product = productService.create(command, loggedInUser())
		render(product.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def update(String id, ProductUpdateCommand command) {
		Product product = productService.update(id, command, loggedInUser())
		boolean isProductOwner = permissionService.check(loggedInUser(), product, Permission.Operation.PRODUCT_SHARE)
		render(product.toMap(isProductOwner) as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def setDeploying(String id) {
		Product product = productService.findById(id, loggedInUser(), Permission.Operation.PRODUCT_EDIT)
		productService.transitionToDeploying(product)
		render(product.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def setPricing(String id, SetPricingCommand command) {
		Product product = apiService.getByIdAndThrowIfNotFound(id)
		productService.updatePricing(product, command, loggedInUser())
		render(product.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def setDeployed(String id, ProductDeployedCommand command) {
		Product product = apiService.getByIdAndThrowIfNotFound(id)
		productService.markAsDeployed(product, command, loggedInUser())
		render(product.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def deployFree(String id) {
		Product product = productService.findById(id, loggedInUser(), Permission.Operation.PRODUCT_SHARE)
		productService.deployFreeProduct(product)
		render(product.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def undeployFree(String id) {
		Product product = productService.findById(id, loggedInUser(), Permission.Operation.PRODUCT_SHARE)
		productService.undeployFreeProduct(product)
		render(product.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def setUndeploying(String id) {
		Product product = productService.findById(id, loggedInUser(), Permission.Operation.PRODUCT_EDIT)
		productService.transitionToUndeploying(product)
		render(product.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def setUndeployed(String id, ProductUndeployedCommand command) {
		Product product = apiService.getByIdAndThrowIfNotFound(id)
		productService.markAsUndeployed(product, command, loggedInUser())
		render(product.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER, expectedContentTypes = ["multipart/form-data"])
	def uploadImage(String id) {
		Product product = productService.findById(id, loggedInUser(), Permission.Operation.PRODUCT_EDIT)
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

	User loggedInUser() {
		request.apiUser
	}
}
