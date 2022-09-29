package com.streamr.core.controller

import com.streamr.core.domain.*
import com.streamr.core.service.NotPermittedException
import com.streamr.core.service.SubscriptionService
import com.streamr.core.service.ValidationException
import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonSlurper
import spock.lang.Specification

@TestFor(SubscriptionApiController)
@Mock([RESTAPIFilters, User, UserRole, Role, Product])
class SubscriptionApiControllerSpec extends Specification {

	User devOpsUser

	void setup() {
		devOpsUser = new User(name: "0x809408D25AC4bF286A665Ba06EaBe0dE57396b37").save(failOnError: true, validate: false)
		def devopsRole = new Role(authority: "ROLE_DEV_OPS").save(failOnError: true)
		new UserRole(user: devOpsUser, role: devopsRole).save(failOnError: true)
	}

	void "index() invokes SubscriptionService#getSubscriptionsOfUser"() {
		def subscriptionService = controller.subscriptionService = Mock(SubscriptionService)
		def user = new User()

		request.apiUser = user

		when:
		withFilters(action: "index") {
			controller.index()
		}

		then:
		1 * subscriptionService.getSubscriptionsOfUser(user)
	}

	void "index() returns 200 and renders subscriptions"() {
		User user = new User(
			username: "0x6E35686A5871d7c18853f99f7442b5b1d0E898A5",
			name: "Firstname Lastname",
		)
		user.id = 1
		user.save(failOnError: true, validate: false)
		def p1 = new Product(
			name: "Product 1",
			description: "description",
			imageUrl: "image1",
			category: new Category(id: "category-1"),
			streams: [],
			owner: user,
			pricePerSecond: "0"
		)

		def p2 = new Product(
			name: "Product 2",
			description: "description",
			imageUrl: "image1",
			category: new Category(id: "category-2"),
			streams: [],
			owner: user,
			pricePerSecond: "0"
		)

		def s1 = new SubscriptionPaid(
			address: "0x0",
			endsAt: new Date(2018, 3, 29, 11, 00, 00),
			product: p1
		)

		def s2 = new SubscriptionPaid(
			address: "0xA",
			endsAt: new Date(2018, 3, 29, 15, 00, 00),
			product: p2
		)

		p1.id = "p1"
		p2.id = "p2"
		s1.id = 10
		s2.id = 20

		controller.subscriptionService = Stub(SubscriptionService) {
			getSubscriptionsOfUser(_) >> [s1, s2]
		}

		when:
		withFilters(action: "index") {
			controller.index()
		}

		then:
		response.status == 200

		and:
		def actualJsonWithoutWeirdNullObjects = new JsonSlurper().parseText((response.json as JSON).toString())
		def expectedJsonWithoutWeirdNullObjects = new JsonSlurper().parseText(([s1, s2]*.toMap() as JSON).toString())
		actualJsonWithoutWeirdNullObjects == expectedJsonWithoutWeirdNullObjects
	}

	void "save() throws ValidationException if request body does not pass validation"() {
		request.apiUser = devOpsUser
		request.JSON = [
			address: "0x0",
			product: "1"
		]
		when:
		withFilters(action: "save") {
			controller.save()
		}
		then:
		thrown(ValidationException)
	}

	void "save() given subscription with address throws NotPermittedException if not devops user"() {
		def product = new Product().save(failOnError: true, validate: false)

		request.apiUser = new User()
		request.JSON = [
			address: "0x0000000000000000000000000000000000000000",
			product: "1",
			endsAt: 1520334404
		]
		when:
		withFilters(action: "save") {
			controller.save()
		}
		then:
		def e = thrown(NotPermittedException)
		e.message.contains("DevOps")
	}

	void "save() given subscription with address invokes subscriptionService#onSubscribed()"() {
		def subscriptionService = controller.subscriptionService = Mock(SubscriptionService)

		def product = new Product().save(failOnError: true, validate: false)

		request.apiUser = devOpsUser
		request.JSON = [
			address: "0x0000000000000000000000000000000000000000",
			product: "1",
			endsAt: 1520334404
		]

		when:
		withFilters(action: "save") {
			controller.save()
		}
		then:
		1 * subscriptionService.onSubscribed(product, "0x0000000000000000000000000000000000000000", _ as Date)
	}

	void "save() given subscription with address returns 204"() {
		controller.subscriptionService = Stub(SubscriptionService)

		def product = new Product().save(failOnError: true, validate: false)

		request.apiUser = devOpsUser
		request.JSON = [
			address: "0x0000000000000000000000000000000000000000",
			product: "1",
			endsAt: 1520334404
		]

		when:
		withFilters(action: "save") {
			controller.save()
		}
		then:
		response.status == 204

	}

	void "save() given subscription without address invokes subscriptionService#onSubscribed()"() {
		def subscriptionService = controller.subscriptionService = Mock(SubscriptionService)

		def product = new Product().save(failOnError: true, validate: false)
		def user = new User()

		request.apiUser = user
		request.JSON = [
			product: "1",
			endsAt: 1520334404
		]

		when:
		withFilters(action: "save") {
			controller.save()
		}
		then:
		1 * subscriptionService.subscribeToFreeProduct(product, user, _ as Date)
	}

	void "save() given subscription without address returns 204"() {
		controller.subscriptionService = Stub(SubscriptionService)

		def product = new Product().save(failOnError: true, validate: false)

		request.apiUser = new User()
		request.JSON = [
			product: "1",
			endsAt: 1520334404
		]

		when:
		withFilters(action: "save") {
			controller.save()
		}
		then:
		response.status == 204

	}
}
