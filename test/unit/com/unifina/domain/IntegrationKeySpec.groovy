package com.unifina.domain

import spock.lang.Specification

class IntegrationKeySpec extends Specification {
	void "toMap().json for Ethereum integration key"() {
		setup:
		IntegrationKey key = new IntegrationKey(
			user: new User(),
			service: IntegrationKey.Service.ETHEREUM,
			json: '{"address": "0xb794F5eA0ba39494cE839613fffBA74279579268", "privateKey": "0x5e98cce00cff5dea6b454889f359a4ec06b9fa6b88e9d69b86de8e1c81887da0"}'
		)

		expect:
		key.toMap().json == [address: "0xb794F5eA0ba39494cE839613fffBA74279579268"]
	}

	void "parse private key from json"() {
		setup:
		IntegrationKey key = new IntegrationKey(
			user: new User(),
			service: IntegrationKey.Service.ETHEREUM,
			json: '{"address": "0xb794F5eA0ba39494cE839613fffBA74279579268", "privateKey": "0x5e98cce00cff5dea6b454889f359a4ec06b9fa6b88e9d69b86de8e1c81887da0"}'
		)

		expect:
		key.parsePrivateKey() == "0x5e98cce00cff5dea6b454889f359a4ec06b9fa6b88e9d69b86de8e1c81887da0"
	}
}
