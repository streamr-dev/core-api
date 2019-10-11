package com.unifina.signalpath.blockchain

import com.unifina.BeanMockingSpecification
import com.unifina.api.NotPermittedException
import com.unifina.datasource.RealtimeDataSource
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import com.unifina.security.StringEncryptor
import com.unifina.service.EthereumIntegrationKeyService
import com.unifina.signalpath.PossibleValue
import com.unifina.signalpath.simplemath.Multiply
import com.unifina.utils.Globals
import grails.test.mixin.Mock

@Mock([IntegrationKey, SecUser])
class EthereumAccountParameterSpec extends BeanMockingSpecification {

	StringEncryptor encryptor
	EthereumAccountParameter parameter

	void setup() {
		def module = new Multiply()
		def keyService = new EthereumIntegrationKeyService()
		keyService.encryptor = encryptor = new StringEncryptor("password")
		mockBean(EthereumIntegrationKeyService, keyService)
		module.globals = new Globals()
		parameter = new EthereumAccountParameter(module, "ethAccount")
	}

	void "right after construction"() {
		expect: "canConnect is false"
		!parameter.canConnect

		and: "value is null"
		parameter.value == null

		and: "getAddress() returns null"
		parameter.getAddress() == null

		and: "getPrivateKey() returns null"
		parameter.getPrivateKey() == null

		and: "possibleValues lists only one placeholder item for null"
		parameter.possibleValues == [new PossibleValue("(none)", null)]
	}

	void "parseValue() returns null given non-existing integration key id"() {
		expect:
		parameter.parseValue("account-1") == null
	}

	void "parseValue() returns null given existing key id but for wrong service"() {
		setup:
		SecUser user = new SecUser(name: "name", username: "name@name.com", password: "pass").save(failOnError: true, validate: false)
		IntegrationKey key = new IntegrationKey(name: "key", service: "WRONG", user: user)
		key.id = "account-1"
		key.json = "{}"
		key.save(failOnError: true, validate: false)

		expect:
		parameter.parseValue("account-1") == null
	}

	void "parseValue() returns integration key given existing Ethereum-service key id"() {
		setup:
		SecUser user = new SecUser(name: "name", username: "name@name.com", password: "pass").save(failOnError: true, validate: false)
		IntegrationKey key = new IntegrationKey(name: "key", service: IntegrationKey.Service.ETHEREUM, user: user, idInService: "0x0")
		key.id = "account-1"
		key.json = "{}"
		key.save(failOnError: true, validate: true)

		expect:
		parameter.parseValue("account-1") != null
	}

	void "getPrivateKey() and getAddress() return values from json, after configuration, if logged in as owner"() {
		setup:
		SecUser user = new SecUser(name: "name", username: "name@name.com", password: "pass").save(failOnError: true, validate: false, flush: true)
		IntegrationKey key = new IntegrationKey(name: "key", service: IntegrationKey.Service.ETHEREUM, user: user, idInService: "0xffff")

		key.id = "account-1"
		key.json = '{ "privateKey": "' + encryptor.encrypt("0000", user.id.byteValue()) + '", "address": "0xffff"}'
		key.save(failOnError: true, validate: true)

		when:
		parameter.setConfiguration([value: "account-1"])
		parameter.getOwner().setGlobals(new Globals([:], user))

		then:
		parameter.getPrivateKey() == "0000"
		parameter.getAddress() == "0xffff"
	}

	void "getAddress() return values from json, after configuration, even if not logged in as user"() {
		setup:
		SecUser user = new SecUser(name: "name", username: "name@name.com", password: "pass").save(failOnError: true, validate: false, flush: true)
		IntegrationKey key = new IntegrationKey(name: "key", service: IntegrationKey.Service.ETHEREUM, user: user, idInService: "0xffff")

		key.id = "account-1"
		key.json = '{"address": "0xa3d1f77acff0060f7213d7bf3c7fec78df847de1", "privateKey": "84de2689ce72c6cd95f15e776eec62369ec7a57e7833ae5454ae05b22d71bb5517360b69f2e5e5879f7c3de8d520361980c50029b18bb7a19d34b2ca4ecc2cac56082e93a9a2e5392665a5943b4acc45bdb29f8c854a901fb25f4476b34f2c25"}'
		key.save(failOnError: true, validate: true)

		Globals globals = new Globals([:], new SecUser())

		when:
		parameter.setConfiguration([value: "account-1"])
		parameter.getOwner().setGlobals(globals)

		then:
		parameter.getAddress() == "0xa3d1f77acff0060f7213d7bf3c7fec78df847de1"
	}

	void "getPrivateKey() throws exception, after configuration, if not logged in as user"() {
		setup:
		SecUser user = new SecUser(name: "name", username: "name@name.com", password: "pass").save(failOnError: true, validate: false, flush: true)
		IntegrationKey key = new IntegrationKey(name: "key", service: IntegrationKey.Service.ETHEREUM, user: user, idInService: 0xffff)

		key.id = "account-1"
		key.json = '{ "privateKey": "0x0000", "address": "0xffff"}'
		key.save(failOnError: true, validate: true)

		when:
		parameter.setConfiguration([value: "account-1"])
		parameter.getOwner().setGlobals(new Globals([:], new SecUser()))

		parameter.getPrivateKey() == "0xffff"

		then:
		thrown(NotPermittedException)
	}

	void "formatValue(null) returns null"() {
		expect:
		parameter.formatValue(null) == null
	}

	void "formatValue(IntegrationKey) returns integration key id"() {
		def key = new IntegrationKey()
		key.id = "id"
		expect:
		parameter.formatValue(key) == "id"
	}

	void "getPossibleValues() returns all Ethereum-service keys of current logged in user"() {
		setup:
		SecUser me = new SecUser(username: "me@me.com")
		SecUser other = new SecUser(username: "other@other.com")
		[me, other]*.save(failOnError: true, validate: false)

		IntegrationKey k1 = new IntegrationKey(name: "key #1", json: '{}', service: IntegrationKey.Service.ETHEREUM, user: me)
		IntegrationKey k2 = new IntegrationKey(name: "key #2", json: '{}', service: "WRONG", user: me)
		IntegrationKey k3 = new IntegrationKey(name: "key #3", json: '{}', service: IntegrationKey.Service.ETHEREUM, user: me)
		IntegrationKey k4 = new IntegrationKey(name: "key #4", json: '{}', service: IntegrationKey.Service.ETHEREUM, user: other)
		[k1, k2, k3, k4]*.save(failOnError: true, validate: false)

		when:
		parameter.getOwner().globals = Stub(Globals) {
			getUserId() >> me.id
		}

		then:
		parameter.possibleValues == [
		    new PossibleValue("(none)", null),
			new PossibleValue("key #1", "1"),
			new PossibleValue("key #3", "3"),
		]
	}

	void "getPossibleValues() contains selected key even if not owner"() {
		setup:
		SecUser me = new SecUser(username: "me@me.com")
		SecUser other = new SecUser(username: "other@other.com")
		[me, other]*.save(failOnError: true, validate: false)

		IntegrationKey otherKey = new IntegrationKey(name: "key #4", json: '{}', service: IntegrationKey.Service.ETHEREUM, user: other)
		otherKey.id = "other-key-id"
		otherKey.save(failOnError: true, validate: false)

		parameter.setConfiguration([value: "other-key-id"])

		when: "logged in as owner of selected key"
		parameter.getOwner().globals = Stub(Globals) {
			getUserId() >> other.id
		}

		then:
		parameter.possibleValues == [
			new PossibleValue("(none)", null),
			new PossibleValue("key #4", "other-key-id")
		]


		when: "logged in as non-owner of selected key"
		parameter.getOwner().globals = Stub(Globals) {
			getUserId() >> me.id
		}

		then:
		parameter.possibleValues == [
			new PossibleValue("(none)", null),
			new PossibleValue("key #4", "other-key-id")
		]
	}
}
