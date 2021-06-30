package com.unifina.service

import com.streamr.client.StreamrClient
import com.streamr.client.authentication.InternalAuthenticationMethod
import com.unifina.domain.User
import com.unifina.utils.testutils.FakeStreamrClient
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@TestFor(StreamrClientService)
@Mock([User])
class StreamrClientServiceSpec extends Specification {
	User user

	void setup() {
		user = new User(
			username: "0x34D239d79Ac9d928547adA6Ba92db54b19688411",
			name: "StreamrClientServiceIntegrationSpec-${System.currentTimeMillis()}@streamr.invalid",
		).save(failOnError: true)

		service.setClientClass(FakeStreamrClient)
	}

	void "getInstanceForThisEngineNode() uses sessionService to generate a sessionToken (instead of making an API call)"() {
		User eeUser = new User()
		SessionToken mockToken = Mock(SessionToken)

		service.ethereumIntegrationKeyService = Mock(EthereumIntegrationKeyService)
		service.sessionService = Mock(SessionService)

		StreamrClient client

		when:
		client = service.getInstanceForThisEngineNode()

		then:
		client.getOptions().getAuthenticationMethod() instanceof InternalAuthenticationMethod
		1 * service.ethereumIntegrationKeyService.getOrCreateFromEthereumAddress(_, _) >> eeUser
		1 * service.sessionService.generateToken(eeUser) >> mockToken
	}

	void "getInstanceForThisEngineNode() should return a singleton instance in a race condition"() {
		User eeUser = new User()
		SessionToken mockToken = Mock(SessionToken)

		service.ethereumIntegrationKeyService = Mock(EthereumIntegrationKeyService)
		service.ethereumIntegrationKeyService.getOrCreateFromEthereumAddress(_, _) >> eeUser

		service.sessionService = Mock(SessionService)
		service.sessionService.generateToken(eeUser) >> mockToken

		List<StreamrClient> instances = Collections.synchronizedList([])
		List<Thread> threads = []

		when:
		// Create race condition
		for (int i = 0; i < 50; i++) {
			Thread t = Thread.start {
				instances.add(service.getInstanceForThisEngineNode())
			}
			threads.add(t)
		}

		then:
		// Wait for all the above threads to finish
		new PollingConditions().within(60) {
			threads.find { it.isAlive() } == null
		}
		// All instances are the same
		instances.unique().size() == 1
	}
}
