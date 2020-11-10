package com.unifina.service

import com.streamr.client.protocol.message_layer.StreamMessage
import com.unifina.domain.Permission
import com.unifina.domain.Product
import com.unifina.domain.Stream
import com.unifina.domain.User
import grails.compiler.GrailsCompileStatic

import java.text.SimpleDateFormat
import java.util.concurrent.ThreadLocalRandom

@GrailsCompileStatic
class ProductService {
	ApiService apiService
	PermissionService permissionService
	SubscriptionService subscriptionService
	DataUnionJoinRequestService dataUnionJoinRequestService
	ProductStore store = new ProductStore()
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
		String formatDate() {
			if (latestMessage == null) {
				return "stream contains no data"
			}
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z")
			df.setTimeZone(TimeZone.getTimeZone("UTC"))
			String date = df.format(latestMessage.getTimestampAsDate())
			return String.format("no data since %s", date)
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
				final StreamMessage msg // = cassandraService.getLatestFromAllPartitions(s)
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

	Map<User, List<ProductService.StaleProduct>> findStaleProductsByOwner(User user) {
		List<Product> products = list(new ProductListParams(publicAccess: true), user)
		List<ProductService.StaleProduct> staleProducts = findStaleProducts(products, new Date())
		Map<User, List<ProductService.StaleProduct>> staleProductsByOwner = staleProducts.groupBy { StaleProduct sp -> sp.product.owner }
		return staleProductsByOwner
	}

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
			product.streams.toArray().each { Stream stream ->
				product.streams.remove(stream)
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
		apiService.list(Product, listParams, currentUser)
	}

	Product findById(String id, User currentUser, Permission.Operation op)
			throws NotFoundException, NotPermittedException {
		apiService.authorizedGetById(Product, id, currentUser, op)
	}

	Product create(CreateProductCommand command, User currentUser)
			throws ValidationException, NotPermittedException {
		if (command.name == null || command.name.trim() == "") {
			command.name = Product.DEFAULT_NAME
		}
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}

		command.streams.each {
			permissionService.verify(currentUser, it, Permission.Operation.STREAM_SHARE)
		}

		Product product = new Product(command.properties)
		product.owner = currentUser
		if ((product.type == Product.Type.DATAUNION) && (product.dataUnionVersion == null)) {
			product.dataUnionVersion = 1
		}
		product.save(failOnError: true)
		permissionService.systemGrantAll(currentUser, product)
		// A stream that is added when creating a new free product should inherit read access for anonymous user
		if (product.isFree()) {
			product.streams.each { stream ->
				permissionService.systemGrantAnonymousAccess(stream, Permission.Operation.STREAM_GET)
				permissionService.systemGrantAnonymousAccess(stream, Permission.Operation.STREAM_SUBSCRIBE)
			}
		}
		return product
	}

	Product update(String id, UpdateProductCommand command, User currentUser) {
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}

		command.streams.each {
			permissionService.verify(currentUser, it, Permission.Operation.STREAM_SHARE)
		}

		Product product = findById(id, currentUser, Permission.Operation.PRODUCT_EDIT)
		Set<Stream> addedStreams = (command.streams as Set<Stream>).findAll{!product.streams.contains(it)}
		Set<Stream> removedStreams = product.streams.findAll{!command.streams.contains(it)}

		command.updateProduct(product, currentUser, permissionService)
		product.save(failOnError: true)

		// A stream that is added when editing an existing free product should inherit read access for anonymous user
		// TODO if a stream is removed from a free product, but still belongs to another free product, we should not
		// remove the permission (this will be fixed in BACK-6)
		if (product.isFree()) {
			Iterable<Permission.Operation> permissions = [Permission.Operation.STREAM_GET, Permission.Operation.STREAM_SUBSCRIBE]
			permissions.each { Permission.Operation permission ->
				addedStreams.each { Stream s ->
					if (!permissionService.checkAnonymousAccess(s, permission)) {
						permissionService.systemGrantAnonymousAccess(s, permission)
					}
				}
				removedStreams.each { Stream s ->
					if (!belongsToFreeProduct(s)) {
						permissionService.systemRevokeAnonymousAccess(s, permission)
					}
				}
			}
		}

		subscriptionService.afterProductUpdated(product)
		return product
	}

	boolean belongsToFreeProduct(Stream s) {
		List<Product> products = store.findProductsByStream(s)
		return products.any{Product p -> p.isFree()}
	}

	void addStreamToProduct(Product product, Stream stream, User currentUser)
			throws ValidationException, NotPermittedException {
		permissionService.verify(currentUser, stream, Permission.Operation.STREAM_SHARE)
		product.streams.add(stream)
		product.save(failOnError: true)
		// A stream that is added when editing an existing free product should inherit read access for anonymous user
		if (product.isFree()) {
			permissionService.systemGrantAnonymousAccess(stream, Permission.Operation.STREAM_GET)
			permissionService.systemGrantAnonymousAccess(stream, Permission.Operation.STREAM_SUBSCRIBE)
		}
		if (product.type == Product.Type.DATAUNION) {
			Set<User> users = dataUnionJoinRequestService.findMembers(product.beneficiaryAddress)
			for (User u : users) {
				if (!permissionService.check(u, stream, Permission.Operation.STREAM_GET)) {
					permissionService.systemGrant(u, stream, Permission.Operation.STREAM_GET)
				}
				if (!permissionService.check(u, stream, Permission.Operation.STREAM_PUBLISH)) {
					permissionService.systemGrant(u, stream, Permission.Operation.STREAM_PUBLISH)
				}
			}
		}
		subscriptionService.afterProductUpdated(product)
	}

	void removeStreamFromProduct(Product product, Stream stream) {
		product.streams.remove(stream)
		product.save(failOnError: true)
		// A stream that is removed when editing an existing free product should revoke read access for anonymous user
		if (product.isFree()) {
			permissionService.systemRevokeAnonymousAccess(stream, Permission.Operation.STREAM_GET)
			permissionService.systemRevokeAnonymousAccess(stream, Permission.Operation.STREAM_SUBSCRIBE)
		}
		if (product.type == Product.Type.DATAUNION) {
			Set<User> users = dataUnionJoinRequestService.findMembers(product.beneficiaryAddress)
			for (User u : users) {
				if (permissionService.check(u, stream, Permission.Operation.STREAM_GET)) {
					permissionService.systemRevoke(u, stream, Permission.Operation.STREAM_GET)
				}
				if (permissionService.check(u, stream, Permission.Operation.STREAM_PUBLISH)) {
					permissionService.systemRevoke(u, stream, Permission.Operation.STREAM_PUBLISH)
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
		product.state = Product.State.DEPLOYED
		product.save(failOnError: true)
		permissionService.systemGrantAnonymousAccess(product, Permission.Operation.PRODUCT_GET)
		return true
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

	private static void verifyDevops(User currentUser) {
		if (!currentUser.isDevOps()) {
			throw new NotPermittedException("DevOps role required")
		}
	}
}
