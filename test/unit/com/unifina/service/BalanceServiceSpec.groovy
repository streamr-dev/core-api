package com.unifina.service

import com.unifina.domain.User
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.runtime.FreshRuntime
import org.web3j.exceptions.MessageDecodingException
import spock.lang.Specification

import java.util.concurrent.ExecutionException

@FreshRuntime
@TestMixin(GrailsUnitTestMixin)
@TestFor(BalanceService)
@Mock([User])
class BalanceServiceSpec extends Specification {
	def address = "0x494531425508c4Bc95E522b24fd571461583E916"

	User me

	static doWithConfig(c) {
		c.streamr.ethereum.datacoinAddress = "0x0000000000000000000000000000000000000001"
	}

	def setup() {
		service.web3jHelperService = Mock(Web3jHelperService)
		me = new User(name: "me", username: address).save(validate: false)
	}

	void "gets balances of keys a user has"() {
		def expected = new BigInteger("100", 16)
		when:
		def result = service.getDatacoinBalances(me)

		then:
		1 * service.web3jHelperService.getERC20Balance(_, _, address) >> expected
		result == [
			(address): expected,
		]
	}

	void "check balance when underlying Web3j API throws InterruptedException"() {
		when:
		service.getDatacoinBalances(me)

		then:
		1 * service.web3jHelperService.getERC20Balance(_, _, address) >> { throw new InterruptedException("mock: thread interrupted") }
		thrown(ApiException)
	}

	void "check balance when underlying Web3j API throws ExecutionException"() {
		when:
		service.getDatacoinBalances(me)

		then:
		1 * service.web3jHelperService.getERC20Balance(_, _, address) >> { throw new ExecutionException("mock: execution aborted", new Exception("root cause")) }
		thrown(ApiException)
	}

	void "check balance when underlying Web3j API throws MessageDecodingException"() {
		when:
		service.getDatacoinBalances(me)

		then:
		1 * service.web3jHelperService.getERC20Balance(_, _, address) >> { throw new MessageDecodingException("mock: message decoding", new Exception("root cause")) }
		thrown(ApiException)
	}
}
