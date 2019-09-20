package com.unifina.service

import com.streamr.client.protocol.message_layer.StreamMessage
import com.unifina.api.*
import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import grails.compiler.GrailsCompileStatic

import java.util.concurrent.ThreadLocalRandom

@GrailsCompileStatic
class ProductService {
	ApiService apiService
	PermissionService permissionService
	SubscriptionService subscriptionService
	CassandraService cassandraService
	CommunityJoinRequestService communityJoinRequestService
	Random random = ThreadLocalRandom.current()

	static class StreamWithLatestMessage {
		Stream stream
		StreamMessage latestMessage
		StreamWithLatestMessage(Stream s, StreamMessage latest) {
			this.stream = s
			this.latestMessage = latest
		}
		String toString() {
			return String.format("StreamWithLatestMessage[stream=%s, latestMessage=%s]", stream, latestMessage)
		}
	}

	static class StaleProduct {
		Product product
		final List<StreamWithLatestMessage> streams = new ArrayList<>()
		StaleProduct(Product p) {
			this.product = p
		}
		String toString() {
			return String.format("StaleProduct[product=%s, streams=%s]", product, streams.toString())
		}
	}

	List<StaleProduct> findStaleProducts(List<Product> products, final Date now) {
		final List<StaleProduct> staleProducts = new ArrayList<>()
		for (Product p : products) {
			StaleProduct stale = new StaleProduct(p)
			for (Stream s : p.getStreams()) {
				if (s.inactivityThresholdHours == 0) {
					continue
				}
				final StreamMessage msg = cassandraService.getLatestFromAllPartitions(s)
				if (msg != null && s.isStale(now, msg.getTimestampAsDate())) {
					stale.streams.add(new StreamWithLatestMessage(s, msg))
				} else if (msg == null) {
					stale.streams.add(new StreamWithLatestMessage(s, null))
				}
			}
			if (!stale.streams.isEmpty()) {
				staleProducts.add(stale)
			}
		}

		return staleProducts
	}

	List<Product> relatedProducts(Product product, int maxResults, SecUser user) {
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
		def user = SecUser.findByUsername(username)
		def all = Product.findAllByOwner(user)
		all.toArray().each { Product product ->
			product.streams.toArray().each { Stream stream ->
				product.streams.remove(stream)
			}
			subscriptionService.deleteProduct(product)
			product.delete(flush: true)
		}
	}

	List<Product> list(ProductListParams listParams, SecUser currentUser) {
		if (listParams.sortBy == null) { // By default, order by score
			listParams.sortBy = "score"
			listParams.order = "desc"
		}
		apiService.list(Product, listParams, currentUser)
	}

	Product findById(String id, SecUser currentUser, Permission.Operation op)
			throws NotFoundException, NotPermittedException {
		apiService.authorizedGetById(Product, id, currentUser, op)
	}

	Product create(CreateProductCommand command, SecUser currentUser)
			throws ValidationException, NotPermittedException {
		if (command.name == null || command.name.trim() == "") {
			command.name = Product.DEFAULT_NAME
		}
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}

		command.streams.each {
			permissionService.verifyShare(currentUser, it)
		}

		Product product = new Product(command.properties)
		product.owner = currentUser
		product.save(failOnError: true)
		permissionService.systemGrantAll(currentUser, product)
		return product
	}

	Product update(String id, UpdateProductCommand command, SecUser currentUser) {
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}

		command.streams.each {
			permissionService.verifyShare(currentUser, it)
		}

		Product product = findById(id, currentUser, Permission.Operation.WRITE)
		command.updateProduct(product, currentUser, permissionService)
		product.save(failOnError: true)
		subscriptionService.afterProductUpdated(product)
		return product
	}

	void addStreamToProduct(Product product, Stream stream, SecUser currentUser)
			throws ValidationException, NotPermittedException {
		permissionService.verifyShare(currentUser, stream)
		product.streams.add(stream)
		product.save(failOnError: true)
		if (product.type == Product.Type.COMMUNITY) {
			Set<SecUser> users = communityJoinRequestService.findCommunityMembers(product.beneficiaryAddress)
			for (SecUser u : users) {
				if (!permissionService.canWrite(u, stream)) {
					permissionService.systemGrant(u, stream, Permission.Operation.WRITE)
				}
			}
		}
		subscriptionService.afterProductUpdated(product)
	}

	void removeStreamFromProduct(Product product, Stream stream) {
		product.streams.remove(stream)
		product.save(failOnError: true)
		if (product.type == Product.Type.COMMUNITY) {
			Set<SecUser> users = communityJoinRequestService.findCommunityMembers(product.beneficiaryAddress)
			for (SecUser u : users) {
				if (permissionService.canWrite(u, stream)) {
					permissionService.systemRevoke(u, stream, Permission.Operation.WRITE)
				}
			}
		}
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

	boolean markAsDeployed(Product product, ProductDeployedCommand command, SecUser currentUser) {
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
		product.state = Product.State.DEPLOYED
		product.save(failOnError: true)
		permissionService.systemGrantAnonymousAccess(product)
		return true
	}

	boolean updatePricing(Product product, SetPricingCommand command, SecUser currentUser) {
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

	boolean markAsUndeployed(Product product, ProductUndeployedCommand command, SecUser currentUser) {
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
		permissionService.systemRevokeAnonymousAccess(product)
		return true
	}

	private static void verifyDevops(SecUser currentUser) {
		if (!currentUser.isDevOps()) {
			throw new NotPermittedException("DevOps role required")
		}
	}
}
