package com.unifina.service

import com.streamr.client.authentication.ApiKeyAuthenticationMethod
import com.unifina.domain.security.Key
import com.unifina.domain.security.SecUser
import com.unifina.utils.testutils.FakeStreamrClient
import spock.lang.Specification

class StreamrClientServiceSpec extends Specification {

	SecUser user
	StreamrClientService service

	void setup() {
		user = new SecUser(
			email: "StreamrClientServiceSpec@streamr.invalid",
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

}
