package com.unifina.service

import com.streamr.client.StreamrClient
import com.streamr.client.authentication.ApiKeyAuthenticationMethod
import com.streamr.client.authentication.InternalAuthenticationMethod
import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import com.unifina.security.SessionToken
import com.unifina.utils.testutils.FakeStreamrClient
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@TestFor(StreamrClientService)
@Mock(SecUser)
class StreamrClientServiceSpec extends Specification {
	SecUser user

	void setup() {
		user = new SecUser(
			username: "StreamrClientServiceIntegrationSpec-${System.currentTimeMillis()}@streamr.invalid",
			name: "user",
			password: "password",
		).save(failOnError: true)

		service.setClientClass(FakeStreamrClient)
	}

	void "getAuthenticatedInstance() should create a StreamrClient with ApiKeyAuthenticationMethod (user has one Key)"() {
		new Key(name: "test", user: user).save(failOnError: true, validate: true)

		when:
		FakeStreamrClient client = (FakeStreamrClient) service.getAuthenticatedInstance(user.id)
		then:
		client.getOptions().getAuthenticationMethod() instanceof ApiKeyAuthenticationMethod
	}

	void "getAuthenticatedInstance() should create a StreamrClient with ApiKeyAuthenticationMethod (user has several Keys)"() {
		new Key(name: "test", user: user).save(failOnError: true, validate: true)
		new Key(name: "test2", user: user).save(failOnError: true, validate: true)
		assert Key.countByUser(user) == 2

		when:
		FakeStreamrClient client = (FakeStreamrClient) service.getAuthenticatedInstance(user.id)
		then:
		client.getOptions().getAuthenticationMethod() instanceof ApiKeyAuthenticationMethod
	}

	void "getAuthenticatedInstance() should throw if a user doesn't have any Keys (illegal state)"() {
		when:
		service.getAuthenticatedInstance(user.id)
		then:
		thrown(IllegalStateException)
	}

	void "getInstanceForThisEngineNode() uses sessionService to generate a sessionToken (instead of making an API call)"() {
		SecUser eeUser = new SecUser()
		SessionToken mockToken = Mock(SessionToken)

		service.ethereumIntegrationKeyService = Mock(EthereumIntegrationKeyService)
		service.sessionService = Mock(SessionService)

		StreamrClient client

		when:
		client = service.getInstanceForThisEngineNode()

		then:
		client.getOptions().getAuthenticationMethod() instanceof InternalAuthenticationMethod
		1 * service.ethereumIntegrationKeyService.getOrCreateFromEthereumAddress(_) >> eeUser
		1 * service.sessionService.generateToken(eeUser) >> mockToken
	}

	void "getInstanceForThisEngineNode() should return a singleton instance in a race condition"() {
		SecUser eeUser = new SecUser()
		SessionToken mockToken = Mock(SessionToken)

		service.ethereumIntegrationKeyService = Mock(EthereumIntegrationKeyService)
		service.ethereumIntegrationKeyService.getOrCreateFromEthereumAddress(_) >> eeUser

		service.sessionService = Mock(SessionService)
		service.sessionService.generateToken(eeUser) >> mockToken

		List<StreamrClient> instances = Collections.synchronizedList([])
		List<Thread> threads = []

		when:
		// Create race condition
		for (int i=0; i<50; i++) {
			Thread t = Thread.start {
				instances.add(service.getInstanceForThisEngineNode())
			}
			threads.add(t)
		}

		then:
		// Wait for all the above threads to finish
		new PollingConditions().within(60) {
			threads.find {it.isAlive()} == null
		}
		// All instances are the same
		instances.unique().size() == 1
	}
}
