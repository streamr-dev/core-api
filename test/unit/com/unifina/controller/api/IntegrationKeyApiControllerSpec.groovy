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
@Mock([UnifinaCoreAPIFilters, Challenge, Key, SecUser])
class IntegrationKeyApiControllerSpec extends Specification {
	EthereumIntegrationKeyService ethereumIntegrationKeyService
	final String signature = "0x50ba6f6df25ba593cb8188df29ca27ea0a7cd38fadc4d40ef9fad455117e190f2a7ec880a76b930071205fee19cf55eb415bd33b2f6cb5f7be36f79f740da6e81b"
	final String name = "foobar"
	final String address = "0x494531425508c4bc95e522b24fd571461583e916"
	final Challenge challenge = new Challenge(
			id: "cc_KtW2PQT-ak4VV2DJJjgF48-j7GPQNejd-1dQENE1A",
			challenge: "This is a challenge created by Streamr to prove private key ownership by signing this random data with it:\n\ncc_KtW2PQT-ak4VV2DJJjgF48-j7GPQNejd-1dQENE1A"
	)
	SecUser me
	void setup() {
		ethereumIntegrationKeyService = controller.ethereumIntegrationKeyService = Mock(EthereumIntegrationKeyService)
		me = new SecUser().save(failOnError: true, validate: false)
		Key key = new Key(name: "key", user: me)
		key.id = "myApiKey"
		key.save(failOnError: true, validate: true)
		challenge.save(failOnError: true, validate: true)
	}

	def "create ethereum id"() {
		when:
		request.addHeader("Authorization", "Token myApiKey")
		request.requestURI = "/api/v1/integration_keys"
		request.method = "POST"
		request.JSON = [
				name     : name,
				service  : "ETHEREUM_ID",
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
				name     : name,
				challenge: [
						id       : challenge.id,
						challenge: challenge.challenge
				],
				signature: signature,
				address  : address
		]
		1 * ethereumIntegrationKeyService.createEthereumID(_, _, _, _, _) >> new IntegrationKey(
				name: name,
				user: me,
				json: ([
						address: new String(address)
				] as JSON).toString(),
				service: IntegrationKey.Service.ETHEREUM_ID.toString()
		)
	}
}
