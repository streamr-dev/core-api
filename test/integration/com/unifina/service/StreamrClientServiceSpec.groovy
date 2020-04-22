package com.unifina.service

import com.streamr.api.client.StreamrClient
import com.streamr.client.authentication.ApiKeyAuthenticationMethod
import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import com.unifina.utils.testutils.FakeStreamrClient
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class StreamrClientServiceSpec extends Specification {

	SecUser user
	StreamrClientService service

	void setup() {
		user = new SecUser(
			username: "StreamrClientServiceSpec@streamr.invalid",
			name: "user",
			password: "password",
		).save(failOnError: true)

		service = new StreamrClientService(FakeStreamrClient)
	}

	void "getAuthenticatedInstance() should create a StreamrClient with ApiKeyAuthenticationMethod (user has one Key)"() {
		new Key(name: "test", user: user).save(failOnError: true, validate: true)

		when:
		FakeStreamrClient client = (FakeStreamrClient) service.getAuthenticatedInstance(user.id)
		then:
		client.getOptionsPassedToConstructor().getAuthenticationMethod() instanceof ApiKeyAuthenticationMethod
	}

	void "getAuthenticatedInstance() should create a StreamrClient with ApiKeyAuthenticationMethod (user has several Keys)"() {
		new Key(name: "test", user: user).save(failOnError: true, validate: true)
		new Key(name: "test2", user: user).save(failOnError: true, validate: true)
		assert Key.countByUser(user) == 2

		when:
		FakeStreamrClient client = (FakeStreamrClient) service.getAuthenticatedInstance(user.id)
		then:
		client.getOptionsPassedToConstructor().getAuthenticationMethod() instanceof ApiKeyAuthenticationMethod
	}

	void "getAuthenticatedInstance() should throw if a user doesn't have any Keys (illegal state)"() {
		when:
		service.getAuthenticatedInstance(user.id)
		then:
		thrown(IllegalStateException)
	}

	void "getInstanceForThisEngineNode() should return a singleton instance in a race condition"() {
		List<StreamrClient> instances = Collections.synchronizedList([])
		List<Thread> threads = []

		// Create race condition
		for (int i=0; i<50; i++) {
			Thread t = Thread.start {
				instances.add(service.getInstanceForThisEngineNode())
			}
			threads.add(t)
		}

		expect:
		// Wait for all the above threads to finish
		new PollingConditions().within(60) {
			threads.find {it.isAlive()} == null
		}
		// All instances are the same
		instances.unique().size() == 1
	}

}
