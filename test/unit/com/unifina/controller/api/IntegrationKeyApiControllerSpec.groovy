package com.unifina.controller.api

import com.unifina.domain.security.Challenge
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import com.unifina.filters.UnifinaCoreAPIFilters
import com.unifina.service.EthereumIntegrationKeyService
import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(IntegrationKeyApiController)
@Mock([UnifinaCoreAPIFilters, Challenge, Key, SecUser, IntegrationKey])
class IntegrationKeyApiControllerSpec extends Specification {
	EthereumIntegrationKeyService ethereumIntegrationKeyService
	SecUser me
	SecUser someoneElse

	void setup() {
		me = new SecUser().save(failOnError: true, validate: false)
		someoneElse = new SecUser().save(failOnError: true, validate: false)

		Key key = new Key(name: "key", user: me)
		key.id = "myApiKey"
		key.save(failOnError: true, validate: true)

		new IntegrationKey(
				name: "my-integration-key-1",
				user: me,
				service: IntegrationKey.Service.ETHEREUM,
				idInService: "0x0000000000000000000",
				json: "{ address: '0x0000000000000000000' }"
		).save(validate: true, failOnError: true)
		new IntegrationKey(
				name: "my-integration-key-2",
				user: me,
				service: IntegrationKey.Service.ETHEREUM_ID,
				idInService: "0x0000000000000000000",
				json: "{ address: '0x0000000000000000000' }"
		).save(validate: true, failOnError: true)
		new IntegrationKey(
				name: "not-my-integration-key",
				user: someoneElse,
				service: IntegrationKey.Service.ETHEREUM,
				idInService: "0x0000000000000000000",
				json: "{ address: '0x0000000000000000000' }"
		).save(validate: true, failOnError: true)
	}

	void "index() lists users integration keys"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/integration_keys"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		response.status == 200
		response.json*.name == ["my-integration-key-1", "my-integration-key-2"]
	}

	void "index() can be filtered by service"() {
		when:
		params.service = "ETHEREUM_ID"
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/integration_keys"
		withFilters(action: "index") {
			controller.index()
		}

		then:
		response.status == 200
		response.json*.name == ["my-integration-key-2"]
	}

	def "create ethereum id"() {
		ethereumIntegrationKeyService = controller.ethereumIntegrationKeyService = Mock(EthereumIntegrationKeyService)
		String address = "0x494531425508c4bc95e522b24fd571461583e916"
		String signature = "0x50ba6f6df25ba593cb8188df29ca27ea0a7cd38fadc4d40ef9fad455117e190f2a7ec880a76b930071205fee19cf55eb415bd33b2f6cb5f7be36f79f740da6e81b"

		Challenge challenge = new Challenge(
			id: "cc_KtW2PQT-ak4VV2DJJjgF48-j7GPQNejd-1dQENE1A",
			challenge: "This is a challenge created by Streamr to prove private key ownership by signing this random data with it:\n\ncc_KtW2PQT-ak4VV2DJJjgF48-j7GPQNejd-1dQENE1A"
		).save(failOnError: true, validate: true)

		when:
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/integration_keys"
		request.method = "POST"
		request.JSON = [
				name     : "foobar",
				service  : IntegrationKey.Service.ETHEREUM_ID.toString(),
				challenge: [
						id       : challenge.id,
						challenge: challenge.challenge
				],
				signature: signature,
				account  : address
		]
		withFilters(action: "save") {
			controller.save()
		}

		then:
		response.status == 201
		response.json == [
				id		 : null,
				challenge: [
					id       : challenge.id,
					challenge: challenge.challenge
				],
				json: [
					address  : address
				],
				name     : "foobar",
				service  : IntegrationKey.Service.ETHEREUM_ID.toString(),
				signature: signature,
		]
		1 * ethereumIntegrationKeyService.createEthereumID(_, _, _, _, _) >> new IntegrationKey(
				name: "foobar",
				user: me,
				json: ([
						address: address
				] as JSON).toString(),
				service: IntegrationKey.Service.ETHEREUM_ID.toString()
		)
	}

	def "delete() invokes ethereumIntegrationKeyService#delete"() {
		ethereumIntegrationKeyService = controller.ethereumIntegrationKeyService = Mock(EthereumIntegrationKeyService)

		when:
		params.id = "integration-key-id"
		request.apiUser = me
		withFilters(action: "delete") {
			controller.delete()
		}

		then:
		1 * ethereumIntegrationKeyService.delete("integration-key-id", me)
	}

	def "delete() responds with 204"() {
		controller.ethereumIntegrationKeyService = Stub(EthereumIntegrationKeyService)

		when:
		params.id = "integration-key-id"
		request.apiUser = me
		withFilters(action: "delete") {
			controller.delete()
		}

		then:
		response.status == 204
	}
}
