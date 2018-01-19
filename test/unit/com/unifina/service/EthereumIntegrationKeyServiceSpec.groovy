package com.unifina.service

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
@Mock([IntegrationKey, SecUser])
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

}
