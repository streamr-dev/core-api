package com.unifina.controller.api

import com.unifina.api.NotPermittedException
import com.unifina.api.ValidationException
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.SecRole
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SecUserSecRole
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.service.SubscriptionService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
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

	void "save() throws NotPermittedException if not devops user"() {
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

	void "save() invokes subscriptionService#onSubscribed() given devops user"() {
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

	void "save() returns 204 given devops user"() {
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
}
