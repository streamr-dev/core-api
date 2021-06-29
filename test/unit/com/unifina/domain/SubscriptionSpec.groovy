package com.unifina.domain

import com.unifina.BeanMockingSpecification
import com.unifina.service.EthereumIntegrationKeyService

class SubscriptionSpec extends BeanMockingSpecification {

	Subscription subscription
	EthereumIntegrationKeyService ethereumIntegrationKeyService

	void setup() {
		subscription = new SubscriptionPaid(address: "0xFAFABCBC00FAFABCBC00FAFABCBC00FAFABCBC00")
		ethereumIntegrationKeyService = mockBean(EthereumIntegrationKeyService, Mock(EthereumIntegrationKeyService))
	}

	void "fetchUser() returns null if no IntegrationKey with address found"() {
		expect:
		subscription.fetchUser() == null
	}

	void "fetchUser() returns user if IntegrationKey with address found"() {
		User user = new User(username: "0xFAFABCBC00FAFABCBC00FAFABCBC00FAFABCBC00").save(failOnError: true, validate: false)
		when:
		User fetched = subscription.fetchUser()
		then:
		1 * ethereumIntegrationKeyService.getEthereumUser("0xFAFABCBC00FAFABCBC00FAFABCBC00FAFABCBC00") >> user
		fetched != null
	}
}
