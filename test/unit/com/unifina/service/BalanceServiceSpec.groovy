package com.unifina.service

import com.unifina.api.ApiException
import grails.test.mixin.TestFor
import org.web3j.exceptions.MessageDecodingException
import spock.lang.Specification

import java.util.concurrent.ExecutionException

@TestFor(BalanceService)
class BalanceServiceSpec extends Specification {
	def address = "0x494531425508c4Bc95E522b24fd571461583E916"
	def setup() {
		service.web3 = Mock(Web3Balance)
    }
    void "check balance happy path"() {
		def expected = new BigInteger("999", 10)

		when:
		def result = service.checkBalance(address)

		then:
		1 * service.web3.checkBalance(address) >> expected
		result == expected
	}
	void "check balance when underlying Web3j API throws InterruptedException"() {
		when:
		service.checkBalance(address)

		then:
		1 * service.web3.checkBalance(address) >> { throw new InterruptedException("mock: thread interrupted") }
		thrown(ApiException)
	}
	void "check balance when underlying Web3j API throws ExecutionException"() {
		when:
		service.checkBalance(address)

		then:
		1 * service.web3.checkBalance(address) >> { throw new ExecutionException("mock: execution aborted", new Exception("root cause")) }
		thrown(ApiException)
	}
	void "check balance when underlying Web3j API throws MessageDecodingException"() {
		when:
		service.checkBalance(address)

		then:
		1 * service.web3.checkBalance(address) >> { throw new MessageDecodingException("mock: message decoding", new Exception("root cause")) }
		thrown(ApiException)
	}
}
