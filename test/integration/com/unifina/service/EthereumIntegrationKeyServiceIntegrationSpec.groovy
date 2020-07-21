package com.unifina.service

import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import spock.lang.Specification

class EthereumIntegrationKeyServiceIntegrationSpec extends Specification {
	void "getEthereumUser query works with lowercase or uppercase input"() {
		setup:
		String ethAddr = "0xF24197f71fC9b2F4F4c24ecE461fB0Ff7C91FD23"
		SecUser me = new SecUser(
			username: "ethereum-ik-spec-1@streamr.com",
			name: "me",
			password: "foo",
		).save(failOnError: true, validate: true)
		IntegrationKey key1 = new IntegrationKey(
			user: me,
			name: "name1",
			json: "{}",
			service: IntegrationKey.Service.ETHEREUM,
			idInService: ethAddr,
		).save(failOnError: true, validate: true)

		SecUser other = new SecUser(
			username: "ethereum-ik-spec-2@streamr.com",
			name: "other",
			password: "bar",
		).save(failOnError: true, validate: true)
		IntegrationKey key2 = new IntegrationKey(
			user: other,
			name: "name2",
			json: "{}",
			service: IntegrationKey.Service.ETHEREUM,
			idInService: "0xD14197f71fC9b2F4F4c24ecE461fB0Ff7C91FD266",
		).save(failOnError: true, validate: true)

		EthereumIntegrationKeyService service = new EthereumIntegrationKeyService()
		when:
		SecUser result = service.getEthereumUser(ethAddr.toLowerCase())
		then:
		result == me
	}
}
