package com.streamr.core.domain
import com.streamr.core.BeanMockingSpecification
import com.streamr.core.service.EthereumUserService
import grails.test.mixin.Mock

@Mock([User, Subscription, SubscriptionPaid])
class SubscriptionSpec extends BeanMockingSpecification {

	Subscription subscription
	EthereumUserService ethereumUserService

	void setup() {
		subscription = new SubscriptionPaid(address: "0xFAFABCBC00FAFABCBC00FAFABCBC00FAFABCBC00")
		ethereumUserService = mockBean(EthereumUserService, Mock(EthereumUserService))
	}

	void "fetchUser() returns null if no User with address found"() {
		expect:
		subscription.fetchUser() == null
	}

	void "fetchUser() returns user if User with address is found"() {
		User user = new User(username: "0xFAFABCBC00FAFABCBC00FAFABCBC00FAFABCBC00").save(failOnError: true, validate: false)
		when:
		User fetched = subscription.fetchUser()
		then:
		1 * ethereumUserService.getEthereumUser("0xFAFABCBC00FAFABCBC00FAFABCBC00FAFABCBC00") >> user
		fetched != null
	}
}
