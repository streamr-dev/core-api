package com.unifina.controller.api

import com.unifina.api.NotPermittedException
import com.unifina.api.ValidationException
import com.unifina.domain.marketplace.Category
import com.unifina.domain.marketplace.PaidSubscription
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.SecRole
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SecUserSecRole
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.service.SubscriptionService
import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonSlurper
import spock.lang.Specification

@TestFor(SubscriptionApiController)
@Mock([UnifinaCoreAPIFilters, SecUser, SecUserSecRole])
class SubscriptionApiControllerSpec extends Specification {

	SecUser devOpsUser

	void setup() {
		devOpsUser = new SecUser(name: "me@streamr.com").save(failOnError: true, validate: false)
		def devopsRole = new SecRole(authority: "ROLE_DEV_OPS").save(failOnError: true)
		new SecUserSecRole(secUser: devOpsUser, secRole:  devopsRole).save(failOnError: true)
	}

	void "index() invokes SubscriptionService#getSubscriptionsOfUser"() {
		def subscriptionService = controller.subscriptionService = Mock(SubscriptionService)
		def user = new SecUser()

		request.apiUser = user

		when:
		withFilters(action: "index") {
			controller.index()
		}

		then:
		1 * subscriptionService.getSubscriptionsOfUser(user)
	}

	void "index() returns 200 and renders subscriptions"() {
		SecUser user = new SecUser(
			username: "user@domain.com",
			name: "Firstname Lastname",
			password: "salasana"
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
			pricePerSecond: 0
		)

		def p2 = new Product(
			name: "Product 2",
			description: "description",
			imageUrl: "image1",
			category: new Category(id: "category-2"),
			streams: [],
			owner: user,
			pricePerSecond: 0
		)

		def s1 = new PaidSubscription(
			address: "0x0",
			endsAt: new Date(2018, 3, 29, 11, 00, 00),
			product: p1
		)

		def s2 = new PaidSubscription(
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
		
		request.apiUser = new SecUser()
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
		def user = new SecUser()

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

		request.apiUser = new SecUser()
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
