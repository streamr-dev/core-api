package com.unifina.service

import com.streamr.client.StreamrClient
import com.streamr.client.authentication.EthereumAuthenticationMethod
import com.streamr.client.authentication.InternalAuthenticationMethod
import com.unifina.domain.IntegrationKey
import com.unifina.domain.User
import com.unifina.security.SessionToken
import com.unifina.utils.testutils.FakeStreamrClient
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@TestFor(StreamrClientService)
@Mock([User, IntegrationKey])
class StreamrClientServiceSpec extends Specification {
	User user

	void setup() {
		user = new User(
			username: "StreamrClientServiceIntegrationSpec-${System.currentTimeMillis()}@streamr.invalid",
			name: "user",
			password: "password",
		).save(failOnError: true)

		service.setClientClass(FakeStreamrClient)
	}

	void "getAuthenticatedInstance() should create a StreamrClient with EthereumAuthenticationMethod (user has one Key)"() {
		setup:
		IntegrationKey key = new IntegrationKey(
			id: "ik-1",
			user: user,
			name: "Test key",
			service: IntegrationKey.Service.ETHEREUM,
			json: '{"address": "0xb794F5eA0ba39494cE839613fffBA74279579268", "privateKey": "0x5e98cce00cff5dea6b454889f359a4ec06b9fa6b88e9d69b86de8e1c81887da0"}',
			idInService: "0xb794F5eA0ba39494cE839613fffBA74279579268",
		).save(failOnError: true, validate: true)
		service.ethereumIntegrationKeyService = Mock(EthereumIntegrationKeyService)
		assert IntegrationKey.countByUser(user) == 1

		when:
		FakeStreamrClient client = (FakeStreamrClient) service.getAuthenticatedInstance(user.id)
		then:
		client.getOptions().getAuthenticationMethod() instanceof EthereumAuthenticationMethod
		1 * service.ethereumIntegrationKeyService.decryptPrivateKey(key) >> "3d1434fe42701a79f8073e1a47d60e57c7b4e951bb663a54f611a54cfc993b3aa8d769afca74aa28f914e3831bfc50b2f120a187021f1abafe703e92ae84f5bfee57d0a79936f0ef87c58d5774a179f15ac715a7bbcc9e357f18d4018e56ac7b"
	}

	void "getAuthenticatedInstance() should create a StreamrClient with EthereumAuthenticationMethod (user has several Keys)"() {
		setup:
		IntegrationKey key = new IntegrationKey(
			id: "ik-1",
			user: user,
			name: "Test key",
			service: IntegrationKey.Service.ETHEREUM,
			json: '{"address": "0xb794F5eA0ba39494cE839613fffBA74279579268", "privateKey": "0x5e98cce00cff5dea6b454889f359a4ec06b9fa6b88e9d69b86de8e1c81887da0"}',
			idInService: "0xb794F5eA0ba39494cE839613fffBA74279579268",
		).save(failOnError: true, validate: true)
		new IntegrationKey(
			id: "ik-2",
			user: user,
			name: "Test key 2",
			service: IntegrationKey.Service.ETHEREUM,
			json: '{"address": "0x222222222ba39494cE839613fffBA74279579268", "privateKey": "0x5e98cce00cff5dea6b454889f359a4ec06b9fa6b88e9d69b86de8e1c81887da0"}',
			idInService: "0x222222222ba39494cE839613fffBA74279579268",
		).save(failOnError: true, validate: true)
		service.ethereumIntegrationKeyService = Mock(EthereumIntegrationKeyService)
		assert IntegrationKey.countByUser(user) == 2

		when:
		FakeStreamrClient client = (FakeStreamrClient) service.getAuthenticatedInstance(user.id)
		then:
		client.getOptions().getAuthenticationMethod() instanceof EthereumAuthenticationMethod
		1 * service.ethereumIntegrationKeyService.decryptPrivateKey(key) >> "3d1434fe42701a79f8073e1a47d60e57c7b4e951bb663a54f611a54cfc993b3aa8d769afca74aa28f914e3831bfc50b2f120a187021f1abafe703e92ae84f5bfee57d0a79936f0ef87c58d5774a179f15ac715a7bbcc9e357f18d4018e56ac7b"
	}

	void "getAuthenticatedInstance() should generate a new integration key if none is found"() {
		setup:
		IntegrationKey key = new IntegrationKey(
			idInService: "0x222222222ba39494cE839613fffBA74279579268",
			service: IntegrationKey.Service.ETHEREUM,
			json: '{"address": "0x222222222ba39494cE839613fffBA74279579268", "privateKey": "0x5e98cce00cff5dea6b454889f359a4ec06b9fa6b88e9d69b86de8e1c81887da0"}',
		)
		service.ethereumIntegrationKeyService = Mock(EthereumIntegrationKeyService)

		when:
		FakeStreamrClient client = (FakeStreamrClient) service.getAuthenticatedInstance(user.id)
		then:
		client.getOptions().getAuthenticationMethod() instanceof EthereumAuthenticationMethod
		1 * service.ethereumIntegrationKeyService.createEthereumAccount(user, "Auto-generated key", _) >> key
		1 * service.ethereumIntegrationKeyService.decryptPrivateKey(key) >> "3d1434fe42701a79f8073e1a47d60e57c7b4e951bb663a54f611a54cfc993b3aa8d769afca74aa28f914e3831bfc50b2f120a187021f1abafe703e92ae84f5bfee57d0a79936f0ef87c58d5774a179f15ac715a7bbcc9e357f18d4018e56ac7b"
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
		1 * service.ethereumIntegrationKeyService.getOrCreateFromEthereumAddress(_ , _) >> eeUser
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
