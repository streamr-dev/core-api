package com.unifina.service

import com.unifina.api.InvalidStateTransitionException
import com.unifina.api.ProductNotFreeException
import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Category
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(FreeProductService)
@Mock([Category, Product, Stream])
class FreeProductServiceSpec extends Specification {
	Stream s1, s2, s3
	Product freeProduct

	void setup() {
		SecUser user = new SecUser(
			username: "user@domain.com",
			name: "Firstname Lastname",
			password: "salasana"
		)
		user.id = 1
		user.save(failOnError: true, validate: false)
		Category category = new Category(name: "Category")
		category.id = "category-id"
		category.save()

		s1 = new Stream(name: "stream-1")
		s2 = new Stream(name: "stream-2")
		s3 = new Stream(name: "stream-3")
		[s1, s2, s3].eachWithIndex { Stream stream, int i -> stream.id = "stream-${i+1}" } // assign ids
		[s1, s2, s3]*.save(failOnError: true, validate: false)

		freeProduct = new Product(
			name: "name",
			description: "description",
			streams: [s1, s2, s3],
			pricePerSecond: 0,
			category: category,
			state: Product.State.NOT_DEPLOYED,
			blockNumber: 40000,
			blockIndex: 30,
			owner: user
		)
		freeProduct.id = "free-product-id"
		freeProduct.save(failOnError: true, validate: true)
	}

	void "deployFreeProduct throws ProductNotFreeException when given paid Product"() {
		when:
		service.deployFreeProduct(new Product(pricePerSecond: 2))
		then:
		thrown(ProductNotFreeException)
	}

	void "deployFreeProduct throws InvalidStateTransitionException when given Product in state DEPLOYED"() {
		when:
		service.deployFreeProduct(new Product(pricePerSecond: 0, state: Product.State.DEPLOYED))
		then:
		thrown(InvalidStateTransitionException)
	}

	void "deployFreeProduct transitions Product state to DEPLOYED"() {
		service.permissionService = Stub(PermissionService)

		when:
		service.deployFreeProduct(freeProduct)
		then:
		Product.findById("free-product-id").state == Product.State.DEPLOYED
	}

	void "deployFreeProduct grants anonymous access to Product"() {
		def permissionService = service.permissionService = Mock(PermissionService)

		when:
		service.deployFreeProduct(freeProduct)
		then:
		1 * permissionService.systemGrantAnonymousAccess(freeProduct)
	}

	void "deployFreeProduct grants anonymous READ access to the streams of Product"() {
		def permissionService = service.permissionService = Mock(PermissionService)

		when:
		service.deployFreeProduct(freeProduct)
		then:
		1 * permissionService.systemGrantAnonymousAccess(s1, Permission.Operation.READ)
		1 * permissionService.systemGrantAnonymousAccess(s2, Permission.Operation.READ)
		1 * permissionService.systemGrantAnonymousAccess(s3, Permission.Operation.READ)
	}

	void "undeployFreeProduct throws ProductNotFreeException when given paid Product"() {
		when:
		service.undeployFreeProduct(new Product(pricePerSecond: 2))
		then:
		thrown(ProductNotFreeException)
	}

	void "undeployFreeProduct throws InvalidStateTransitionException when given Product in state NOT_DEPLOYED"() {
		when:
		service.undeployFreeProduct(new Product(pricePerSecond: 0, state: Product.State.NOT_DEPLOYED))
		then:
		thrown(InvalidStateTransitionException)
	}

	void "undeployFreeProduct transitions Product state to NOT_DEPLOYED"() {
		service.permissionService = Stub(PermissionService)

		freeProduct.state = Product.State.DEPLOYED
		freeProduct.save(failOnError: true)

		when:
		service.undeployFreeProduct(freeProduct)
		then:
		Product.findById("free-product-id").state == Product.State.NOT_DEPLOYED
	}

	void "undeployFreeProduct revokes anonymous access to Product"() {
		def permissionService = service.permissionService = Mock(PermissionService)

		freeProduct.state = Product.State.DEPLOYED
		freeProduct.save(failOnError: true)

		when:
		service.undeployFreeProduct(freeProduct)
		then:
		1 * permissionService.systemRevokeAnonymousAccess(freeProduct)
	}

	void "undeployFreeProduct revokes anonymous READ access to the streams of Product"() {
		def permissionService = service.permissionService = Mock(PermissionService)

		freeProduct.state = Product.State.DEPLOYED
		freeProduct.save(failOnError: true)

		when:
		service.undeployFreeProduct(freeProduct)
		then:
		1 * permissionService.systemRevokeAnonymousAccess(s1, Permission.Operation.READ)
		1 * permissionService.systemRevokeAnonymousAccess(s2, Permission.Operation.READ)
		1 * permissionService.systemRevokeAnonymousAccess(s3, Permission.Operation.READ)
	}
}
