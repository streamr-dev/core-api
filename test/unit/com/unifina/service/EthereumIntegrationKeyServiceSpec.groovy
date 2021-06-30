package com.unifina.service

import com.unifina.domain.SignupMethod
import com.unifina.domain.User
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import org.web3j.crypto.ECKeyPair
import spock.lang.Specification

@TestMixin(ControllerUnitTestMixin)
@TestFor(EthereumIntegrationKeyService)
@Mock([User])
class EthereumIntegrationKeyServiceSpec extends Specification {
	String address = "0x8eEEF384734a8cEfeC53eA49eb651D0257cbA6B6"
	User me

	ChallengeService challengeService
	SubscriptionService subscriptionService
	PermissionService permissionService

	void setup() {
		me = new User(username: address).save(failOnError: true, validate: false)
		permissionService = service.permissionService = Mock(PermissionService)
	}

	void "get address from private key"() {
		setup:
		ECKeyPair keyPair = ECKeyPair.create("0x0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF".getBytes())
		expect:
		EthereumIntegrationKeyService.getAddress(keyPair.privateKey.toString()) == "37aa29f21ccb6a280830ccbefdbb40b9f5b08b34"
	}

	void "create user checks for duplicate address"() {
		when:
		service.createEthereumUser(address, SignupMethod.UNKNOWN)
		then:
		thrown(DuplicateNotAllowedException)
	}

	void "getOrCreateFromEthereumAddress() creates user if key does not exists"() {
		User someoneElse = new User(username: "0x7328Ac6F6ce7442Baa695dB8f1Fc442a01eA3056").save(failOnError: true, validate: false)
		when:
		service.getOrCreateFromEthereumAddress(address, SignupMethod.UNKNOWN)
		then:
		User.count == 2
	}

	void "getOrCreateFromEthereumAddress() returns user if key exists"() {
		when:
		User user = service.getOrCreateFromEthereumAddress(address, SignupMethod.UNKNOWN)
		then:
		user.username == me.username
		User.count == 1
	}
}
