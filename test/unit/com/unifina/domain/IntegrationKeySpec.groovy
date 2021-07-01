package com.unifina.domain

import spock.lang.Specification

class IntegrationKeySpec extends Specification {
	void "toMap().json for Ethereum integration key"() {
		setup:
		IntegrationKey key = new IntegrationKey(
			user: new User(),
			service: IntegrationKey.Service.ETHEREUM_ID,
			json: '{"address": "0xb794F5eA0ba39494cE839613fffBA74279579268"}'
		)

		expect:
		key.toMap().json == [address: "0xb794F5eA0ba39494cE839613fffBA74279579268"]
	}
}
