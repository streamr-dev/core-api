package com.unifina.domain.security

import com.unifina.service.EthereumIntegrationKeyService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(EthereumIntegrationKeyService)
@Mock([SecUser])
class IntegrationKeySpec extends Specification {
	def "toMap().json for Ethereum integration key"() {
		def key = new IntegrationKey(
			user: new SecUser(username: "me@me.com").save(failOnError: true, validate: false),
			service: IntegrationKey.Service.ETHEREUM,
			json: '{"address": "0xa3d1f77acff0060f7213d7bf3c7fec78df847de1", "privateKey": "84de2689ce72c6cd95f15e776eec62369ec7a57e7833ae5454ae05b22d71bb5517360b69f2e5e5879f7c3de8d520361980c50029b18bb7a19d34b2ca4ecc2cac56082e93a9a2e5392665a5943b4acc45bdb29f8c854a901fb25f4476b34f2c25"}'
		)

		expect:
		key.toMap().json == [address: "0xa3d1f77acff0060f7213d7bf3c7fec78df847de1", privateKey: "0x5e98cce00cff5dea6b454889f359a4ec06b9fa6b88e9d69b86de8e1c81887da0"]
	}
}
