package com.unifina.service

import com.unifina.domain.User
import com.unifina.utils.Web3jWrapper
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(ENSService)
@Mock(User)
class ENSServiceSpec extends Specification {
	void setup() {
		service.web3jWrapper = Mock(Web3jWrapper)
	}

	void "ethereum user is required"() {
		setup:
		User user = new User(username: "user@example.com")
		when:
		service.isENSOwnedBy("streamr.eth", user)
		then:
		thrown(EthereumUserRequiredException)
	}

    void "returns true when the owner of domain matches ENS record"() {
		setup:
		String address = "0x96482bdfc1f42f7f1c907388e36c75ffd2aa5866"
		User user = new User(username: address)
		String domain = "streamr.eth"

		when:
		boolean result = service.isENSOwnedBy(domain, user)
		then:
		1 * service.web3jWrapper.getENSDomainOwner(domain) >> address
		result
    }

	void "returns false when the owner of domain doesnt match ENS record"() {
		setup:
		User user = new User(username: "0x96482bdfc1f42f7f1c907388e36c75ffd2aa5866")
		String domain = "streamr.eth"

		when:
		boolean result = service.isENSOwnedBy(domain, user)
		then:
		1 * service.web3jWrapper.getENSDomainOwner(domain) >> "0x00002bdfc1f42f7f1c907388e36c75ffd2aa0000"
		!result
	}
}
