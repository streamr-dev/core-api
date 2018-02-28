package com.unifina.service

import com.unifina.api.ApiException
import com.unifina.domain.security.Challenge
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import groovy.json.JsonSlurper
import spock.lang.Shared
import spock.lang.Specification

@TestMixin(ControllerUnitTestMixin) // "as JSON" converter
@TestFor(EthereumIntegrationKeyService)
@Mock([IntegrationKey, SecUser, Challenge])
class EthereumIntegrationKeyServiceSpec extends Specification {

	@Shared String actualPassword
	SecUser me

	void setupSpec() {
		actualPassword = grailsApplication.config.streamr.encryption.password
		grailsApplication.config.streamr.encryption.password = "password"
	}

	void cleanupSpec() {
		grailsApplication.config.streamr.encryption.password = actualPassword
	}

	void setup() {
		me = new SecUser(username: "me@me.com").save(failOnError: true, validate: false)
		service.init()
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
		e.message == "Private key must be a valid hex string!"
	}

	void "createEthereumAccount creates expected integration key"() {
		when:
		def integrationKey = service.createEthereumAccount(me,
			"ethKey",
			"    " + "fa7d31d2fb3ce6f18c629857b7ef5cc3c6264dc48ddf6557cc20cf7a5b361365" + "	 "
		)

		then:
		integrationKey.toMap() == [
		    id: "1",
			user: 1,
			name: "ethKey",
			service: "ETHEREUM",
			json: [
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
		def other = new SecUser(username: "other@other.com").save(failOnError: true, validate: false)
		def k1 = service.createEthereumAccount(me,
			"ethKey 1",
			"    " + "fa7d31d2fb3ce6f18c629857b7ef5cc3c6264dc48ddf6557cc20cf7a5b361365" + "	 "
		)
		def k2 = service.createEthereumAccount(me,
			"ethKey 2",
			"    " + "fa7d31d2fb3ce6f18c629857b7ef5cc3c6264dc48ddf6557cc20cf7a5b361365" + "	 "
		)
		def k3 = service.createEthereumAccount(other,
			"ethKey 3",
			"    " + "fa7d41d2fb3ce6f18c629857b7ef5cc3c6264dc48ddf6557cc20cf7a5b361365" + "	 "
		)

		when:
		def keys = service.getAllKeysForUser(me)
		then:
		keys == [k1, k2]
	}

	void "createEthereumID() creates IntegrationKey with proper values"() {
		def ch = new Challenge(challenge: "foobar")
		ch.save(failOnError: true, validate: false)
		final String signature = "0x50ba6f6df25ba593cb8188df29ca27ea0a7cd38fadc4d40ef9fad455117e190f2a7ec880a76b930071205fee19cf55eb415bd33b2f6cb5f7be36f79f740da6e81b"
		final String address = "0x10705c0b408eb64860f67a81f5908b51b62a86fc"
		final String name = "foobar"
		when:
		IntegrationKey key = service.createEthereumID(me, name, ch.id, ch.challenge, signature)
		Map json = new JsonSlurper().parseText(key.json)
		then:
		key.name == name
		json.containsKey("address")
		json.address == address
	}

	void "createEthereumID() validates challenge"() {
		final String signature = "0x50ba6f6df25ba593cb8188df29ca27ea0a7cd38fadc4d40ef9fad455117e190f2a7ec880a76b930071205fee19cf55eb415bd33b2f6cb5f7be36f79f740da6e81b"
		when:
		service.createEthereumID(me, "name", "", "", signature)
		then:
		def e = thrown(ApiException)
		e.message == "challenge validation failed"
	}
}
