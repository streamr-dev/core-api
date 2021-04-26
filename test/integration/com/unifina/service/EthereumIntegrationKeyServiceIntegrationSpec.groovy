package com.unifina.service

import com.unifina.domain.IntegrationKey
import com.unifina.domain.User
import spock.lang.Specification

class EthereumIntegrationKeyServiceIntegrationSpec extends Specification {
	void "getEthereumUser query works with lowercase or uppercase input"() {
		setup:
		String ethAddr = "0xF24197f71fC9b2F4F4c24ecE461fB0Ff7C91FD23"
		User me = new User(
			username: "ethereum-ik-spec-1@streamr.network",
			name: "me",
		).save(failOnError: true, validate: true)
		IntegrationKey key1 = new IntegrationKey(
			user: me,
			name: "name1",
			json: "{}",
			service: IntegrationKey.Service.ETHEREUM_ID,
			idInService: ethAddr,
		).save(failOnError: true, validate: true)

		User other = new User(
			username: "ethereum-ik-spec-2@streamr.network",
			name: "other",
		).save(failOnError: true, validate: true)
		IntegrationKey key2 = new IntegrationKey(
			user: other,
			name: "name2",
			json: "{}",
			service: IntegrationKey.Service.ETHEREUM_ID,
			idInService: "0xD14197f71fC9b2F4F4c24ecE461fB0Ff7C91FD266",
		).save(failOnError: true, validate: true)

		EthereumIntegrationKeyService service = new EthereumIntegrationKeyService()
		when:
		User result = service.getEthereumUser(ethAddr.toLowerCase())
		then:
		result == me
	}
}
