package com.unifina.service

import com.unifina.domain.IntegrationKey
import com.unifina.domain.SignupMethod
import com.unifina.domain.User
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import groovy.json.JsonSlurper
import org.web3j.crypto.ECKeyPair
import spock.lang.Specification

@TestMixin(ControllerUnitTestMixin)
@TestFor(EthereumIntegrationKeyService)
@Mock([IntegrationKey, User])
class EthereumIntegrationKeyServiceSpec extends Specification {

	User me

	ChallengeService challengeService
	UserService userService
	SubscriptionService subscriptionService
	PermissionService permissionService

	void setup() {
		me = new User(username: "me@me.com").save(failOnError: true, validate: false)
		challengeService = service.challengeService = Mock(ChallengeService)
		userService = service.userService = Mock(UserService)
		subscriptionService = service.subscriptionService = Mock(SubscriptionService)
		permissionService = service.permissionService = Mock(PermissionService)
	}

	void "get address from private key"() {
		setup:
		ECKeyPair keyPair = ECKeyPair.create("0x0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF".getBytes())
		expect:
		EthereumIntegrationKeyService.getAddress(keyPair.privateKey.toString()) == "37aa29f21ccb6a280830ccbefdbb40b9f5b08b34"
	}

	void "createEthereumID() creates IntegrationKey with proper values"() {
		service.subscriptionService = Stub(SubscriptionService)

		def ch = new Challenge("id", "foobar", 300)
		final String signature = "0x50ba6f6df25ba593cb8188df29ca27ea0a7cd38fadc4d40ef9fad455117e190f2a7ec880a76b930071205fee19cf55eb415bd33b2f6cb5f7be36f79f740da6e81b"
		final String address = "0x10705c0b408eb64860f67a81f5908b51b62a86fc"
		final String name = "foobar"
		when:
		IntegrationKey key = service.createEthereumID(me, name, ch.getId(), ch.getChallenge(), signature)
		Map json = new JsonSlurper().parseText(key.json)
		then:
		1 * challengeService.verifyChallengeAndGetAddress(ch.getId(), ch.getChallenge(), signature) >> address
		key.name == name
		json.containsKey("address")
		json.address == address
	}

	void "createEthereumID() invokes subscriptionService#afterIntegrationKeyCreated when integration key created"() {
		def subscriptionService = service.subscriptionService = Mock(SubscriptionService)

		def ch = new Challenge("id", "foobar", 300)
		final String signature = "0x50ba6f6df25ba593cb8188df29ca27ea0a7cd38fadc4d40ef9fad455117e190f2a7ec880a76b930071205fee19cf55eb415bd33b2f6cb5f7be36f79f740da6e81b"
		final String address = "0x10705c0b408eb64860f67a81f5908b51b62a86fc"
		final String name = "foobar"

		when:
		service.createEthereumID(me, name, ch.getId(), ch.getChallenge(), signature)
		then:
		1 * challengeService.verifyChallengeAndGetAddress(ch.getId(), ch.getChallenge(), signature) >> address
		1 * subscriptionService.afterIntegrationKeyCreated({ it.id != null })
	}

	void "createEthereumID() checks for duplicate addresses"() {
		service.subscriptionService = Stub(SubscriptionService)
		String signature = "0x50ba6f6df25ba593cb8188df29ca27ea0a7cd38fadc4d40ef9fad455117e190f2a7ec880a76b930071205fee19cf55eb415bd33b2f6cb5f7be36f79f740da6e81b"
		String name = "foobar"

		def ch = new Challenge("id", "foobar", 300)

		when:
		service.createEthereumID(me, name, ch.getId(), ch.getChallenge(), signature)
		service.createEthereumID(me, name, ch.getId(), ch.getChallenge(), signature)

		then:
		2 * challengeService.verifyChallengeAndGetAddress(ch.getId(), ch.getChallenge(), signature) >> "address"
		thrown(DuplicateNotAllowedException)
	}

	void "delete() deletes matching IntegrationKey"() {
		service.subscriptionService = Stub(SubscriptionService)

		IntegrationKey extraKey = new IntegrationKey(
			user: me,
			service: IntegrationKey.Service.ETHEREUM_ID,
		)
		extraKey.id = "extra-key"
		extraKey.save(failOnError: true, validate: false)

		IntegrationKey integrationKey = new IntegrationKey(
			user: me,
			service: IntegrationKey.Service.ETHEREUM_ID,
		)
		integrationKey.id = "integration-key"
		integrationKey.save(failOnError: true, validate: false)

		when:
		service.delete("integration-key", me)
		then:
		IntegrationKey.count() == 1
	}

	void "delete() invokes subscriptionService#beforeIntegrationKeyRemoved for Ethereum IDs"() {
		def subscriptionService = service.subscriptionService = Mock(SubscriptionService)

		IntegrationKey extraKey = new IntegrationKey(
			user: me,
			service: IntegrationKey.Service.ETHEREUM_ID,
		)
		extraKey.id = "extra-key"
		extraKey.save(failOnError: true, validate: false)

		IntegrationKey integrationKey = new IntegrationKey(user: me)
		integrationKey.id = "integration-key"
		integrationKey.service = IntegrationKey.Service.ETHEREUM_ID
		integrationKey.save(failOnError: true, validate: false)

		when:
		service.delete("integration-key", me)
		then:
		1 * subscriptionService.beforeIntegrationKeyRemoved(integrationKey)
	}

	void "delete() does not delete matching IntegrationKey if not owner"() {
		User someoneElse = new User(username: "someoneElse@streamr.network").save(failOnError: true, validate: false)

		IntegrationKey extraKey = new IntegrationKey(
			user: someoneElse,
			service: IntegrationKey.Service.ETHEREUM_ID,
		)
		extraKey.id = "extra-key"
		extraKey.save(failOnError: true, validate: false)
		IntegrationKey extraKey2 = new IntegrationKey(
			user: someoneElse,
			service: IntegrationKey.Service.ETHEREUM_ID,
		)
		extraKey2.id = "extra-key-2"
		extraKey2.save(failOnError: true, validate: false)

		IntegrationKey integrationKey = new IntegrationKey(
			user: me,
			service: IntegrationKey.Service.ETHEREUM_ID,
		)
		integrationKey.id = "integration-key"
		integrationKey.save(failOnError: true, validate: false)

		when:
		service.delete("integration-key", someoneElse)
		then:
		IntegrationKey.count() == 3
	}

	void "delete() does not invoke subscriptionService#beforeIntegrationKeyRemoved if not found"() {
		def subscriptionService = service.subscriptionService = Mock(SubscriptionService)
		IntegrationKey extraKey = new IntegrationKey(
			user: me,
			service: IntegrationKey.Service.ETHEREUM_ID,
		)
		extraKey.id = "extra-key"
		extraKey.save(failOnError: true, validate: false)
		IntegrationKey extraKey2 = new IntegrationKey(
			user: me,
			service: IntegrationKey.Service.ETHEREUM_ID,
		)
		extraKey2.id = "extra-key-2"
		extraKey2.save(failOnError: true, validate: false)

		when:
		service.delete("integration-key", me)
		then:
		0 * subscriptionService._
	}

	void "delete() does not invoke subscriptionService#beforeIntegrationKeyRemoved if not owner"() {
		def subscriptionService = service.subscriptionService = Mock(SubscriptionService)
		User someoneElse = new User(username: "someoneElse@streamr.network").save(failOnError: true, validate: false)

		IntegrationKey extraKey = new IntegrationKey(
			user: someoneElse,
			service: IntegrationKey.Service.ETHEREUM_ID,
		)
		extraKey.id = "extra-key"
		extraKey.save(failOnError: true, validate: false)
		IntegrationKey extraKey2 = new IntegrationKey(
			user: someoneElse,
			service: IntegrationKey.Service.ETHEREUM_ID,
		)
		extraKey2.id = "extra-key-2"
		extraKey2.save(failOnError: true, validate: false)

		IntegrationKey integrationKey = new IntegrationKey(
			user: me,
			service: IntegrationKey.Service.ETHEREUM_ID,
		)
		integrationKey.id = "integration-key"
		integrationKey.save(failOnError: true, validate: false)

		when:
		service.delete("integration-key", someoneElse)
		then:
		0 * subscriptionService._
	}

	void "getOrCreateFromEthereumAddress() creates user if key does not exists"() {
		User someoneElse = new User(username: "someoneElse@streamr.network").save(failOnError: true, validate: false)
		when:
		service.getOrCreateFromEthereumAddress("address", SignupMethod.UNKNOWN)
		then:
		1 * userService.createUser(_) >> someoneElse
		IntegrationKey.count == 1
		User.count == 2
	}

	void "getOrCreateFromEthereumAddress() returns user if key exists"() {
		String address = "someEthereumAdddress"
		IntegrationKey integrationKey = new IntegrationKey(
			user: me,
			idInService: address,
			service: IntegrationKey.Service.ETHEREUM_ID
		).save(failOnError: true, validate: false)

		when:
		User user = service.getOrCreateFromEthereumAddress(address, SignupMethod.UNKNOWN)
		then:
		user.username == me.username
		IntegrationKey.count == 1
		User.count == 1
	}

	void "cannot remove only key of ethereum user"() {
		when:
		String address = "0x26e1ae3f5efe8a01eca8c2e9d3c32702cf4bead6"
		User user = new User(username: address).save(failOnError: true, validate: false)
		IntegrationKey integrationKey = new IntegrationKey(
			user: user,
			idInService: address,
			service: IntegrationKey.Service.ETHEREUM_ID
		).save(failOnError: true, validate: false)
		service.delete(integrationKey.id, user)
		then:
		thrown CannotRemoveEthereumKeyException
	}
}
