package com.unifina.domain.security

import spock.lang.Specification

class IntegrationKeySpec extends Specification {
	def "toMap().json for Ethereum integration key"() {
		def key = new IntegrationKey(
			user: new SecUser(),
			service: IntegrationKey.Service.ETHEREUM,
			json: '{"address": "0xb794F5eA0ba39494cE839613fffBA74279579268", "privateKey": "0000000000000000000000000"}'
		)

		expect:
		key.toMap().json == [address: "0xb794F5eA0ba39494cE839613fffBA74279579268"]
	}
}
