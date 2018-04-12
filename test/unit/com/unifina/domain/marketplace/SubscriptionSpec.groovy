package com.unifina.domain.marketplace

import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import grails.test.mixin.Mock
import spock.lang.Specification

@Mock([IntegrationKey])
class SubscriptionSpec extends Specification {

	Subscription subscription

	void setup() {
		subscription = new PaidSubscription(address: "0xFAFABCBC00FAFABCBC00FAFABCBC00FAFABCBC00")
	}

	void "getUser() returns null if no IntegrationKey with address found"() {
		expect:
		subscription.fetchUser() == null
	}

	void "getUser() returns null if IntegrationKey with address found but IntegrationKey.service != ETHEREUM_ID"() {
		setup:
		new IntegrationKey(
				user: new SecUser(username: "me@streamr.com").save(failOnError: true, validate: false),
				name: "integration key",
				service: IntegrationKey.Service.ETHEREUM,
				json: "{}",
				idInService: "0xFAFABCBC00FAFABCBC00FAFABCBC00FAFABCBC00"
		).save(failOnError: true, validate: true)

		expect:
		subscription.fetchUser() == null
	}

	void "getUser() returns user if IntegrationKey with address found and IntegrationKey.service == ETHEREUM_ID"() {
		setup:
		new IntegrationKey(
				user: new SecUser(username: "me@streamr.com").save(failOnError: true, validate: false),
				name: "integration key",
				service: IntegrationKey.Service.ETHEREUM_ID,
				json: "{}",
				idInService: "0xFAFABCBC00FAFABCBC00FAFABCBC00FAFABCBC00"
		).save(failOnError: true, validate: true)

		expect:
		subscription.fetchUser() != null
	}
}
