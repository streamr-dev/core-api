package com.unifina.domain

import com.unifina.BeanMockingSpecification
import com.unifina.service.EthereumIntegrationKeyService
import grails.test.mixin.Mock

@Mock([IntegrationKey])
class SubscriptionSpec extends BeanMockingSpecification {

	Subscription subscription
	EthereumIntegrationKeyService ethereumIntegrationKeyService

	void setup() {
		subscription = new PaidSubscription(address: "0xFAFABCBC00FAFABCBC00FAFABCBC00FAFABCBC00")
		ethereumIntegrationKeyService = mockBean(EthereumIntegrationKeyService, Mock(EthereumIntegrationKeyService))
	}

	void "fetchUser() returns null if no IntegrationKey with address found"() {
		expect:
		subscription.fetchUser() == null
	}

	void "fetchUser() returns user if IntegrationKey with address found"() {
		User user = new User(username: "me@streamr.com").save(failOnError: true, validate: false)
		new IntegrationKey(
			user: user,
			name: "integration key",
			service: IntegrationKey.Service.ETHEREUM_ID,
			json: "{}",
			idInService: "0xFAFABCBC00FAFABCBC00FAFABCBC00FAFABCBC00"
		).save(failOnError: true, validate: true)
		when:
		User fetched = subscription.fetchUser()
		then:
		1 * ethereumIntegrationKeyService.getEthereumUser("0xFAFABCBC00FAFABCBC00FAFABCBC00FAFABCBC00") >> user
		fetched != null
	}
}
