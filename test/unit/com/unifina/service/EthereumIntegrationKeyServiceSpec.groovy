package com.unifina.service

import com.unifina.api.CannotRemoveEthereumKeyException
import com.unifina.api.DuplicateNotAllowedException
import com.unifina.domain.Stream
import com.unifina.domain.Permission
import com.unifina.domain.SignupMethod
import com.unifina.security.Challenge
import com.unifina.domain.IntegrationKey
import com.unifina.domain.User
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import groovy.json.JsonSlurper
import spock.lang.Shared
import spock.lang.Specification

@TestMixin(ControllerUnitTestMixin)
// "as JSON" converter
@TestFor(EthereumIntegrationKeyService)
@Mock([IntegrationKey, User])
class EthereumIntegrationKeyServiceSpec extends Specification {

	@Shared
	String actualPassword
	User me

	ChallengeService challengeService
	UserService userService
	SubscriptionService subscriptionService
	PermissionService permissionService

	void setupSpec() {
		actualPassword = grailsApplication.config.streamr.encryption.password
		grailsApplication.config.streamr.encryption.password = "password"
	}

	void cleanupSpec() {
		grailsApplication.config.streamr.encryption.password = actualPassword
	}

	void setup() {
		me = new User(username: "me@me.com").save(failOnError: true, validate: false)
		service.init()
		challengeService = service.challengeService = Mock(ChallengeService)
		userService = service.userService = Mock(UserService)
		subscriptionService = service.subscriptionService = Mock(SubscriptionService)
		permissionService = service.permissionService = Mock(PermissionService)
	}

	void "init() without grailsConfig streamr.encryption.password throws IllegalArgumentException"() {
		when:
		grailsApplication.config.streamr.encryption.password = null
		service.init()

		then:
		def e = thrown(IllegalArgumentException)
		e.message =~ "streamr.encryption.password"

		cleanup:
		grailsApplication.config.streamr.encryption.password = "password"
	}

	void "createEthereumAccount throws IllegalArgumentException when given non-hex private key"() {
		when:
		service.createEthereumAccount(me, "ethKey", "THIS IS NOT A PRIVATE KEY")

		then:
		def e = thrown(IllegalArgumentException)
		e.message == "The private key must be a hex string of 64 chars (without the 0x prefix)."
	}

	void "createEthereumAccount creates expected integration key"() {
		when:
		def integrationKey = service.createEthereumAccount(me,
			"ethKey",
			"    " + "fa7d31d2fb3ce6f18c629857b7ef5cc3c6264dc48ddf6557cc20cf7a5b361365" + "	 "
		)

		then:
		integrationKey.toMap() == [
			id     : "1",
			user   : 1,
			name   : "ethKey",
			service: "ETHEREUM",
			json   : [
				address: "0xf4f683a8502b2796392bedb05dbbcc8c6e582e59"
			]
		]
	}

	void "createEthereumAccount encrypts private key"() {
		when:
		def integrationKey = service.createEthereumAccount(me,
			"ethKey",
			"    " + "fa7d31d2fb3ce6f18c629857b7ef5cc3c6264dc48ddf6557cc20cf7a5b361365" + "	 "
		)
		Map json = new JsonSlurper().parseText(integrationKey.json)

		then:
		json.containsKey("privateKey")
		json.privateKey != "fa7d31d2fb3ce6f18c629857b7ef5cc3c6264dc48ddf6557cc20cf7a5b361365"

	}

	void "decryptPrivateKey provides clear text private key from IntegrationKey"() {
		def integrationKey = service.createEthereumAccount(me,
			"ethKey",
			"    " + "fa7d31d2fb3ce6f18c629857b7ef5cc3c6264dc48ddf6557cc20cf7a5b361365" + "	 "
		)

		expect:
		service.decryptPrivateKey(integrationKey) == "fa7d31d2fb3ce6f18c629857b7ef5cc3c6264dc48ddf6557cc20cf7a5b361365"
	}

	void "getAllKeysForUser fetches all Ethereum keys for user"() {
		def other = new User(username: "other@other.com").save(failOnError: true, validate: false)
		def k1 = service.createEthereumAccount(me,
			"ethKey 1",
			"    " + "fa7d31d2fb3ce6f18c629857b7ef5cc3c6264dc48ddf6557cc20cf7a5b361365" + "	 "
		)
		def k2 = service.createEthereumAccount(me,
			"ethKey 2",
			"    " + "fa7d31d2fb3ce6f18c629857b7ef5cc3c6264dc48ddf6557cc20cf7a5b361366" + "	 "
		)
		def k3 = service.createEthereumAccount(other,
			"ethKey 3",
			"    " + "fa7d41d2fb3ce6f18c629857b7ef5cc3c6264dc48ddf6557cc20cf7a5b361367" + "	 "
		)

		when:
		def keys = service.getAllPrivateKeysForUser(me)
		then:
		keys == [k1, k2]
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

		def integrationKey = new IntegrationKey(user: me)
		integrationKey.id = "integration-key"
		integrationKey.save(failOnError: true, validate: false)

		when:
		service.delete("integration-key", me)
		then:
		IntegrationKey.count() == 0
	}

	void "delete() deletes corresponding inbox stream and its permissions"() {
		service.subscriptionService = Stub(SubscriptionService)

		def integrationKey = new IntegrationKey(user: me)
		integrationKey.id = "integration-key"
		integrationKey.idInService = "address"
		integrationKey.save(failOnError: true, validate: false)
		Stream inbox = new Stream()
		inbox.id = "address"
		inbox.inbox = true
		inbox.save(failOnError: true, validate: false)
		Permission perm = new Permission(operation: Permission.Operation.STREAM_SUBSCRIBE)
		perm.stream = inbox
		perm.user = me
		perm.save(failOnError: true, validate: false)

		when:
		service.delete("integration-key", me)
		then:
		IntegrationKey.count() == 0
		Stream.get("address") == null
		Permission.findAllByStream(inbox) == []
	}

	void "delete() invokes subscriptionService#beforeIntegrationKeyRemoved for Ethereum IDs"() {
		def subscriptionService = service.subscriptionService = Mock(SubscriptionService)

		def integrationKey = new IntegrationKey(user: me)
		integrationKey.id = "integration-key"
		integrationKey.service = IntegrationKey.Service.ETHEREUM_ID
		integrationKey.save(failOnError: true, validate: false)

		when:
		service.delete("integration-key", me)
		then:
		1 * subscriptionService.beforeIntegrationKeyRemoved(integrationKey)
	}

	void "delete() does not delete matching IntegrationKey if not owner"() {
		def integrationKey = new IntegrationKey(user: me)
		integrationKey.id = "integration-key"
		integrationKey.save(failOnError: true, validate: false)

		User someoneElse = new User(username: "someoneElse@streamr.com").save(failOnError: true, validate: false)

		when:
		service.delete("integration-key", someoneElse)
		then:
		IntegrationKey.count() == 1
	}

	void "delete() does not invoke subscriptionService#beforeIntegrationKeyRemoved if not found"() {
		def subscriptionService = service.subscriptionService = Mock(SubscriptionService)

		when:
		service.delete("integration-key", me)
		then:
		0 * subscriptionService._
	}

	void "delete() does not invoke subscriptionService#beforeIntegrationKeyRemoved if not owner"() {
		def subscriptionService = service.subscriptionService = Mock(SubscriptionService)

		def integrationKey = new IntegrationKey(user: me)
		integrationKey.id = "integration-key"
		integrationKey.save(failOnError: true, validate: false)

		User someoneElse = new User(username: "someoneElse@streamr.com").save(failOnError: true, validate: false)

		when:
		service.delete("integration-key", someoneElse)
		then:
		0 * subscriptionService._
	}

	void "getOrCreateFromEthereumAddress() creates user if key does not exists"() {
		User someoneElse = new User(username: "someoneElse@streamr.com").save(failOnError: true, validate: false)
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

	void "createEthereumUser() creates inbox stream"() {
		User someoneElse = new User(username: "someoneElse@streamr.com").save(failOnError: true, validate: false)
		when:
		service.createEthereumUser("address", SignupMethod.UNKNOWN)
		then:
		1 * userService.createUser(_) >> someoneElse
		1 * permissionService.systemGrantAll(_, _)
		Stream.count == 1
		Stream.get("address").inbox
	}

	void "createEthereumID() creates inbox stream"() {
		service.subscriptionService = Stub(SubscriptionService)

		Challenge ch = new Challenge("id", "foobar", 300)
		final String signature = "0x50ba6f6df25ba593cb8188df29ca27ea0a7cd38fadc4d40ef9fad455117e190f2a7ec880a76b930071205fee19cf55eb415bd33b2f6cb5f7be36f79f740da6e81b"
		final String address = "0x10705c0b408eb64860f67a81f5908b51b62a86fc"
		final String name = "foobar"
		when:
		service.createEthereumID(me, name, ch.getId(), ch.getChallenge(), signature)
		then:
		1 * permissionService.systemGrantAll(_, _)
		1 * challengeService.verifyChallengeAndGetAddress(ch.getId(), ch.getChallenge(), signature) >> address
		Stream.count == 1
		Stream.get(address).inbox
	}

	void "createEthereumAccount() creates inbox stream"() {
		when:
		service.createEthereumAccount(me, "ethKey", "fa7d31d2fb3ce6f18c629857b7ef5cc3c6264dc48ddf6557cc20cf7a5b361365")
		then:
		1 * permissionService.systemGrantAll(_, _)
		Stream.count == 1
		Stream.get("0xf4f683a8502b2796392bedb05dbbcc8c6e582e59").inbox
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
