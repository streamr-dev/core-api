package com.unifina.controller.api

import com.unifina.api.*
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.security.AllowRole
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import com.unifina.service.*
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import grails.plugin.mail.MailService
import org.apache.log4j.Logger
import org.springframework.web.multipart.MultipartFile

class ProductApiController {
	ApiService apiService
	FreeProductService freeProductService
	ProductService productService
	ProductImageService productImageService
	MailService mailService
	PermissionService permissionService

	private static final Logger log = Logger.getLogger(ProductApiController)

	@GrailsCompileStatic
	@StreamrApi(allowRoles = AllowRole.ADMIN)
	def staleProducts() {
		List<Product> products = productService.list(new ProductListParams(publicAccess: true), loggedInUser())
		List<ProductService.StaleProduct> results = productService.findStaleProducts(products, new Date())
		return render(results as JSON)
	}

	@StreamrApi(allowRoles = AllowRole.ADMIN)
	def emailStaleProductOwners() {
		Boolean dryRun = params.boolean("dry_run") ?: false
		Map<SecUser, List<ProductService.StaleProduct>> staleProductsByOwner = productService.findStaleProductsByOwner(loggedInUser())
		for (Map.Entry<SecUser, List<ProductService.StaleProduct>> entry : staleProductsByOwner.entrySet()) {
			SecUser owner = entry.getKey()
			List<ProductService.StaleProduct> ownersProducts = entry.getValue()
			if (!owner.isEthereumUser()) {
				if (dryRun) {
					log.info(String.format("dry run: sending stale product email to %s", owner.username))
				} else {
					log.info(String.format("sending stale product email to %s", owner.username))
					try {
						mailService.sendMail {
							from grailsApplication.config.unifina.email.sender
							to owner.username
							subject "Problem with your products on Streamr Marketplace"
							html g.render(template: "/emails/email_stale_product_notification", model: [user: owner, staleProducts: ownersProducts])
						}
					} catch (Exception e) {
						log.error(String.format("send stale product email to %s failed: ", owner.username), e)
					}
				}
			}
		}
		return render(status: 204)
	}

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
		apiService.addLinkHintToHeader(listParams, products.size(), params, response)
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
	def save(CreateProductCommand command) {
		Product product = productService.create(command, loggedInUser())
		render(product.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def update(String id, UpdateProductCommand command) {
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
		Product product = productService.findById(id, loggedInUser(), Permission.Operation.PRODUCT_SHARE)
		freeProductService.deployFreeProduct(product)
		render(product.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def undeployFree(String id) {
		Product product = productService.findById(id, loggedInUser(), Permission.Operation.PRODUCT_SHARE)
		freeProductService.undeployFreeProduct(product)
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
		Product product = apiService.getByIdAndThrowIfNotFound(Product, id)
		productService.markAsUndeployed(product, command, loggedInUser())
		render(product.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
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

	SecUser loggedInUser() {
		request.apiUser
	}
}
