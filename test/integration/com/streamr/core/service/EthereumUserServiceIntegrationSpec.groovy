package com.streamr.core.service

import com.streamr.core.domain.User
import com.streamr.core.service.EthereumUserService
import spock.lang.Specification

class EthereumUserServiceIntegrationSpec extends Specification {
	void "getEthereumUser query works with lowercase or uppercase input"() {
		setup:
		String ethAddr = "0xF24197f71fC9b2F4F4c24ecE461fB0Ff7C91FD23"
		User me = new User(
			username: ethAddr,
			name: "ethereum-ik-spec-1@streamr.network",
		).save(failOnError: true, validate: true)

		User other = new User(
			username: "0xD14197f71fC9b2F4F4c24ecE461fB0Ff7C91FD26",
			name: "ethereum-ik-spec-2@streamr.network",
		).save(failOnError: true, validate: true)

        EthereumUserService service = new EthereumUserService()
		when:
		User result = service.getEthereumUser(ethAddr.toLowerCase())
		then:
		result == me
	}
}
