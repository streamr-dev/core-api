package com.streamr.core.service

import com.streamr.core.domain.Permission
import com.streamr.core.domain.Product
import com.streamr.core.domain.User
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import spock.lang.Specification

@TestMixin(ControllerUnitTestMixin)
@TestFor(ApiService)
@Mock([Permission, Product])
class ApiServiceSpec extends Specification {

	void "list() returns streams with share permission"() {
		def permissionService = service.permissionService = Mock(PermissionService)
		ListParams listParams = new ProductListParams(operation: Permission.Operation.PRODUCT_SHARE.toString(), publicAccess: true)
		User me = new User(username: "0x0000000000000000000000000000000000000001")

		when:
		def list = service.list(listParams, me)

		then:
		list.size() == 3
		1 * permissionService.get(me, Permission.Operation.PRODUCT_SHARE, true, _) >> [
			new Product(), new Product(), new Product()
		]
	}

	void "list() delegates to permissionService#get"() {
		def permissionService = service.permissionService = Mock(PermissionService)

		User me = new User(username: "0x0000000000000000000000000000000000000001")
		ListParams listParams = new ProductListParams(publicAccess: true)

		when:
		def list = service.list(listParams, me)

		then:
		list.size() == 3
		1 * permissionService.get(me, Permission.Operation.PRODUCT_GET, true, _) >> [
			new Product(), new Product(), new Product()
		]
	}

	void "list() passes user as null to permissionService#get if grantedAccess=false"() {
		def permissionService = service.permissionService = Mock(PermissionService)

		User me = new User(username: "0x0000000000000000000000000000000000000001")
		ListParams listParams = new ProductListParams(publicAccess: true, grantedAccess: false)

		when:
		service.list(listParams, me)

		then:
		1 * permissionService.get(null, _, _, _)
	}

	void "list() invokes listParams#validate and listParams#createListCriteria and passes returned closure to permissionService#get"() {
		def permissionService = service.permissionService = Mock(PermissionService)

		User me = new User(username: "0x0000000000000000000000000000000000000001")
		ListParams listParams = Mock(ListParams)

		when:
		service.list(listParams, me)

		then:
		1 * permissionService.get(_, _, _, { it() == "see me?" })
		1 * listParams.validate() >> true
		1 * listParams.createListCriteria() >> { { a -> "see me?" } }
	}

	void "list() throws ValidationException if validation of listParams fails"() {
		User me = new User(username: "0x0000000000000000000000000000000000000001")
		ListParams listParams = new ProductListParams(order: null)

		when:
		service.list(listParams, me)
		then:
		thrown(ValidationException)
	}

	void "getByIdAndThrowIfNotFound() throws NotFoundException if domain object cannot be found"() {
		when:
		service.getByIdAndThrowIfNotFound("product-id")

		then:
		def e = thrown(NotFoundException)
		e.asApiError().toMap() == [
			id: "product-id",
			message: "Product with id product-id not found",
			code: "NOT_FOUND",
			fault: "id",
			type: "Product"
		]
	}

	void "getByIdAndThrowIfNotFound() returns domain object if it exists"() {
		Product product = new Product(name: "product")
		product.id = "product-id"
		product.owner = new User(username: "0x0000000000000000000000000000000000000001")
		product.save(failOnError: true)

		expect:
		service.getByIdAndThrowIfNotFound("product-id") == product
	}

	void "authorizedGetById() throws NotFoundException if domain object cannot be found"() {
		User me = new User(username: "0x0000000000000000000000000000000000000001")

		when:
		service.authorizedGetById("product-id", me, Permission.Operation.PRODUCT_EDIT)

		then:
		def e = thrown(NotFoundException)
		e.asApiError().toMap() == [
			id: "product-id",
			message: "Product with id product-id not found",
			code: "NOT_FOUND",
			fault: "id",
			type: "Product"
		]
	}

	void "authorizedGetById() throws NotPermittedException if user does not have required permission"() {
		User me = new User(username: "0x0000000000000000000000000000000000000001")
		Product product = new Product(name: "product")
		product.id = "product-id"
		product.owner = me
		product.save(failOnError: true)

		service.permissionService = new PermissionService()

		when:
		service.authorizedGetById("product-id", me, Permission.Operation.PRODUCT_EDIT)

		then:
		def e = thrown(NotPermittedException)
		e.message == "0x0000000000000000000000000000000000000001 does not have permission to product_edit Product (id product-id)"
	}

	void "authorizedGetById() returns domain object if it exists and user has required permission"() {
		User me = new User(username: "0x0000000000000000000000000000000000000001")
		Product product = new Product(name: "product")
		product.id = "product-id"
		product.owner = me
		product.save(failOnError: true)

		service.permissionService = Stub(PermissionService) // replace verify() with nop method

		when:
		def result = service.authorizedGetById("product-id", me, Permission.Operation.PRODUCT_EDIT)

		then:
		result == product
	}
}
