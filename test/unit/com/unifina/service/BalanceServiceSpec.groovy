package com.unifina.service

import com.unifina.api.ApiException
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.runtime.FreshRuntime
import org.web3j.exceptions.MessageDecodingException
import spock.lang.Specification

import java.util.concurrent.ExecutionException

@FreshRuntime
@TestMixin(GrailsUnitTestMixin)
@TestFor(BalanceService)
@Mock([SecUser, IntegrationKey])
class BalanceServiceSpec extends Specification {
	def address = "0x494531425508c4Bc95E522b24fd571461583E916"
	def address2 = "0x0000000000000000000000000000000000000001"

	SecUser me

	static doWithConfig(c) {
		c.streamr.ethereum.datacoinAddress = "0x0000000000000000000000000000000000000001"
	}

	def setup() {
		service.web3jHelperService = Mock(Web3jHelperService)
		me = new SecUser(name: "me", email: "me@too.com").save(validate: false)
    }

    void "gets balances of keys a user has"() {
		def expected = new BigInteger("100", 16)
		def expected2 = new BigInteger("200", 16)

		new IntegrationKey(
			user: me,
			service: IntegrationKey.Service.ETHEREUM,
			idInService: address,
		).save(validate: false)
		new IntegrationKey(
			user: me,
			service: IntegrationKey.Service.ETHEREUM,
			idInService: address2,
		).save(validate: false)

		when:
		def result = service.getDatacoinBalances(me)

		then:
		1 * service.web3jHelperService.getERC20Balance(_, _, address) >> expected
		1 * service.web3jHelperService.getERC20Balance(_, _, address2) >> expected2
		result == [
			(address): expected,
			(address2): expected2,
		]
	}

	void "returns empty map if user has no keys"() {
		when:
		def result = service.getDatacoinBalances(me)

		then:
		result == [:]
	}

	void "check balance when underlying Web3j API throws InterruptedException"() {
		new IntegrationKey(
			user: me,
			service: IntegrationKey.Service.ETHEREUM,
			idInService: address,
		).save(validate: false)

		when:
		service.getDatacoinBalances(me)

		then:
		1 * service.web3jHelperService.getERC20Balance(_, _, address) >> { throw new InterruptedException("mock: thread interrupted") }
		thrown(ApiException)
	}
	void "check balance when underlying Web3j API throws ExecutionException"() {
		new IntegrationKey(
			user: me,
			service: IntegrationKey.Service.ETHEREUM,
			idInService: address,
		).save(validate: false)

		when:
		service.getDatacoinBalances(me)

		then:
		1 * service.web3jHelperService.getERC20Balance(_, _, address) >> { throw new ExecutionException("mock: execution aborted", new Exception("root cause")) }
		thrown(ApiException)
	}
	void "check balance when underlying Web3j API throws MessageDecodingException"() {
		new IntegrationKey(
			user: me,
			service: IntegrationKey.Service.ETHEREUM,
			idInService: address,
		).save(validate: false)

		when:
		service.getDatacoinBalances(me)

		then:
		1 * service.web3jHelperService.getERC20Balance(_, _, address) >> { throw new MessageDecodingException("mock: message decoding", new Exception("root cause")) }
		thrown(ApiException)
	}
}
