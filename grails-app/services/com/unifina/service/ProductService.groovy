package com.unifina.service

import com.unifina.domain.Permission
import com.unifina.domain.Product
import com.unifina.domain.User
import grails.compiler.GrailsCompileStatic

import java.util.concurrent.ThreadLocalRandom

@GrailsCompileStatic
class ProductService {
	ApiService apiService
	PermissionService permissionService
	SubscriptionService subscriptionService
	ProductStore store = new ProductStore()
	Random random = ThreadLocalRandom.current()

	List<Product> relatedProducts(Product product, int maxResults, User user) {
		// find Product.owner's other products
		ListParams params = new ProductListParams(productOwner: product.owner, max: maxResults, publicAccess: true)
		Set<Product> all = new HashSet<Product>(list(params, user))

		// find other products from the same category
		params = new ProductListParams(categories: [product.category].toSet(), max: maxResults, publicAccess: true)
		all.addAll(list(params, user))

		// remove given product itself from the set (if it is there)
		all.remove(product)

		def relatedProducts = new ArrayList<Product>(all)

		while (relatedProducts.size() > maxResults) {
			int i = random.nextInt(relatedProducts.size() - 1)
			relatedProducts.remove(i)
		}
		return relatedProducts
	}

	void removeUsersProducts(String username) {
		def user = User.findByUsername(username)
		def all = Product.findAllByOwner(user)
		all.toArray().each { Product product ->
			product.streams.toArray().each { String streamId ->
				product.streams.remove(streamId)
			}
			subscriptionService.deleteProduct(product)
			product.delete(flush: true)
		}
	}

	List<Product> list(ProductListParams listParams, User currentUser) {
		if (listParams.sortBy == null) { // By default, order by score
			listParams.sortBy = "score"
			listParams.order = "desc"
		}
		apiService.list(listParams, currentUser)
	}

	Product findById(String id, User currentUser, Permission.Operation op)
		throws NotFoundException, NotPermittedException {
		apiService.authorizedGetById(id, currentUser, op)
	}

	Product create(ProductCreateCommand command, User currentUser)
		throws ValidationException, NotPermittedException {
		if (command.name == null || command.name.trim() == "") {
			command.name = Product.DEFAULT_NAME
		}
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}

		command.streams.each { String streamId ->
			// TODO: permissionService.verify(currentUser, streamId, Permission.Operation.STREAM_SHARE)
		}

		Product product = new Product(command.properties)
		product.owner = currentUser
		product.save(failOnError: true)
		permissionService.systemGrantAll(currentUser, product)
		return product
	}

	Product update(String id, ProductUpdateCommand command, User currentUser) {
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}

		command.streams.each { String streamId ->
			// TODO: permissionService.verify(currentUser, streamId, Permission.Operation.STREAM_SHARE)
		}

		Product product = findById(id, currentUser, Permission.Operation.PRODUCT_EDIT)
		Set<String> addedStreams = (command.streams as Set<String>).findAll { !product.streams.contains(it) }
		Set<String> removedStreams = product.streams.findAll { !command.streams.contains(it) }

		command.updateProduct(product, currentUser, permissionService)
		product.save(failOnError: true)

		subscriptionService.afterProductUpdated(product)
		return product
	}

	boolean belongsToFreeProduct(String s) {
		List<Product> products = store.findProductsByStream(s)
		return products.any { Product p -> p.isFree() }
	}

	void addStreamToProduct(Product product, String streamId, User currentUser)
		throws ValidationException, NotPermittedException {
		product.streams.add(streamId)
		product.save(failOnError: true)
		subscriptionService.afterProductUpdated(product)
	}

	void removeStreamFromProduct(Product product, String streamId) {
		product.streams.remove(streamId)
		product.save(failOnError: true)
		subscriptionService.afterProductUpdated(product)
	}

	void transitionToDeploying(Product product) {
		if (product.state == Product.State.NOT_DEPLOYED) {
			product.state = Product.State.DEPLOYING
			product.save(failOnError: true)
		} else {
			throw new InvalidStateTransitionException(product.state, Product.State.DEPLOYING)
		}
	}

	boolean markAsDeployed(Product product, ProductDeployedCommand command, User currentUser) {
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}
		if (command.isStale(product)) {
			return false
		}
		if (product.state == Product.State.UNDEPLOYING) {
			throw new InvalidStateTransitionException(product.state, Product.State.DEPLOYED)
		}
		verifyDevops(currentUser)

		product.setProperties(command.properties)
		saveAndGrantPermission(Product.State.DEPLOYED, product)
		return true
	}

	private void saveAndGrantPermission(Product.State state, Product product) {
		product.state = state
		product.save(failOnError: true)
		permissionService.systemGrantAnonymousAccess(product, Permission.Operation.PRODUCT_GET)
	}

	boolean updatePricing(Product product, SetPricingCommand command, User currentUser) {
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}
		if (command.isStale(product)) {
			return false
		}
		if (product.state == Product.State.UNDEPLOYING) {
			throw new InvalidStateTransitionException(product.state, Product.State.DEPLOYED)
		}
		verifyDevops(currentUser)

		product.setProperties(command.properties)
		product.save(failOnError: true)
		return product
	}

	void transitionToUndeploying(Product product) {
		if (product.state == Product.State.DEPLOYED) {
			product.state = Product.State.UNDEPLOYING
			product.save(failOnError: true)
		} else {
			throw new InvalidStateTransitionException(product.state, Product.State.UNDEPLOYING)
		}
	}

	boolean markAsUndeployed(Product product, ProductUndeployedCommand command, User currentUser) {
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}
		if (command.isStale(product)) {
			return false
		}
		if (product.state in [Product.State.DEPLOYING, Product.State.NOT_DEPLOYED]) {
			throw new InvalidStateTransitionException(product.state, Product.State.NOT_DEPLOYED)
		}
		verifyDevops(currentUser)

		product.setProperties(command.properties)
		product.state = Product.State.NOT_DEPLOYED
		product.save(failOnError: true)
		permissionService.systemRevokeAnonymousAccess(product, Permission.Operation.PRODUCT_GET)
		return true
	}

	Product findByBeneficiaryAddress(String beneficiaryAddress) {
		return (Product) Product.createCriteria().get {
			ilike("beneficiaryAddress", beneficiaryAddress)
			// ilike = case-insensitive like: Ethereum addresses are case-insensitive but different case systems are in use (checksum-case, lower-case at least)
		}
	}

	private static void verifyDevops(User currentUser) {
		if (!currentUser.isDevOps()) {
			throw new NotPermittedException("DevOps role required")
		}
	}

	// Free products

	void deployFreeProduct(Product product) {
		verifyThatProductIsFree(product)
		if (product.state == Product.State.DEPLOYED) { // TODO: state transition mismatch with paid products
			throw new InvalidStateTransitionException(product.state, Product.State.DEPLOYED)
		}

		saveAndGrantPermission(Product.State.DEPLOYED, product)
	}

	void undeployFreeProduct(Product product) {
		verifyThatProductIsFree(product)
		if (product.state == Product.State.NOT_DEPLOYED) { // TODO: state transition mismatch with paid products
			throw new InvalidStateTransitionException(product.state, Product.State.NOT_DEPLOYED)
		}

		product.state = Product.State.NOT_DEPLOYED
		product.save(failOnError: true)
		permissionService.systemRevokeAnonymousAccess(product, Permission.Operation.PRODUCT_GET)
	}

	static void verifyThatProductIsFree(Product product) {
		if (!product.isFree()) {
			throw new ProductNotFreeException(product)
		}
	}
}
