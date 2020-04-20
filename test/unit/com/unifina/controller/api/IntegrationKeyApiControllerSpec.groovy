package com.unifina.controller.api

import com.unifina.ControllerSpecification
import com.unifina.api.ApiException
import com.unifina.api.NotFoundException
import com.unifina.security.Challenge
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import com.unifina.service.EthereumIntegrationKeyService
import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(IntegrationKeyApiController)
@Mock([Key, SecUser, IntegrationKey])
class IntegrationKeyApiControllerSpec extends ControllerSpecification {
	EthereumIntegrationKeyService ethereumIntegrationKeyService
	SecUser me
	SecUser someoneElse

	def setup() {
		ethereumIntegrationKeyService = mockBean(EthereumIntegrationKeyService, Mock(EthereumIntegrationKeyService))
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
			json: '{"address": "0xa3d1f77acff0060f7213d7bf3c7fec78df847de1", "privateKey": "84de2689ce72c6cd95f15e776eec62369ec7a57e7833ae5454ae05b22d71bb5517360b69f2e5e5879f7c3de8d520361980c50029b18bb7a19d34b2ca4ecc2cac56082e93a9a2e5392665a5943b4acc45bdb29f8c854a901fb25f4476b34f2c25"}'
		).save(validate: true, failOnError: true)
		new IntegrationKey(
			name: "my-integration-key-2",
			user: me,
			service: IntegrationKey.Service.ETHEREUM_ID,
			idInService: "0x0000000000000000000",
			json: '{"address": "0xa3d1f77acff0060f7213d7bf3c7fec78df847de1", "privateKey": "84de2689ce72c6cd95f15e776eec62369ec7a57e7833ae5454ae05b22d71bb5517360b69f2e5e5879f7c3de8d520361980c50029b18bb7a19d34b2ca4ecc2cac56082e93a9a2e5392665a5943b4acc45bdb29f8c854a901fb25f4476b34f2c25"}'
		).save(validate: true, failOnError: true)
		new IntegrationKey(
			name: "not-my-integration-key",
			user: someoneElse,
			service: IntegrationKey.Service.ETHEREUM,
			idInService: "0x0000000000000000000",
			json: '{"address": "0xa3d1f77acff0060f7213d7bf3c7fec78df847de1", "privateKey": "84de2689ce72c6cd95f15e776eec62369ec7a57e7833ae5454ae05b22d71bb5517360b69f2e5e5879f7c3de8d520361980c50029b18bb7a19d34b2ca4ecc2cac56082e93a9a2e5392665a5943b4acc45bdb29f8c854a901fb25f4476b34f2c25"}'
		).save(validate: true, failOnError: true)
	}

	void "index() lists users integration keys"() {
		when:
		authenticatedAs(me) { controller.index() }

		then:
		response.status == 200
		response.json*.name == ["my-integration-key-1", "my-integration-key-2"]
	}

	void "index() can be filtered by service"() {
		when:
		params.service = "ETHEREUM_ID"
		authenticatedAs(me) { controller.index() }

		then:
		response.status == 200
		response.json*.name == ["my-integration-key-2"]
	}

	def "create ethereum id"() {
		ethereumIntegrationKeyService = controller.ethereumIntegrationKeyService = Mock(EthereumIntegrationKeyService)
		String address = "0x494531425508c4bc95e522b24fd571461583e916"
		String signature = "0x50ba6f6df25ba593cb8188df29ca27ea0a7cd38fadc4d40ef9fad455117e190f2a7ec880a76b930071205fee19cf55eb415bd33b2f6cb5f7be36f79f740da6e81b"

		Challenge challenge = new Challenge("cc_KtW2PQT-ak4VV2DJJjgF48-j7GPQNejd-1dQENE1A",
			"This is a challenge created by Streamr to prove private key ownership by signing this random data with it:\n\n",
			300)

		when:
		request.method = "POST"
		request.JSON = [
			name     : "foobar",
			service  : IntegrationKey.Service.ETHEREUM_ID.toString(),
			challenge: [
				id       : challenge.getId(),
				challenge: challenge.getChallenge()
			],
			signature: signature,
			account  : address
		]
		authenticatedAs(me) { controller.save() }

		then:
		response.status == 201
		response.json == [
			id		 : null,
			challenge: [
				id       : challenge.getId(),
				challenge: challenge.getChallenge()
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
		String id = "integration-key-id"
		request.requestURI = "/api/v1/integration_keys/" + id
		request.method = "DELETE"
		request.apiUser = me
		authenticatedAs(me) { controller.delete(id) }

		then:
		1 * ethereumIntegrationKeyService.delete("integration-key-id", me)
	}

	def "delete() responds with 204"() {
		controller.ethereumIntegrationKeyService = Stub(EthereumIntegrationKeyService)

		when:
		String id = "integration-key-id"
		request.requestURI = "/api/v1/integration_keys/" + id
		request.method = "DELETE"
		request.apiUser = me
		authenticatedAs(me) { controller.delete(id) }

		then:
		response.status == 204
	}

	def "update integration key name"() {
		setup:
		controller.ethereumIntegrationKeyService = Mock(EthereumIntegrationKeyService)

		when:
		String id = "1234"
		request.requestURI = "/api/v1/integration_keys/" + id
		request.method = "PUT"
		request.json = [
		    name: "key's new name",
		]
		request.apiUser = me
		authenticatedAs(me) { controller.update(id) }

		then:
		1 * controller.ethereumIntegrationKeyService.updateKey(me, "1234", "key's new name")
		response.status == 204
	}
}
