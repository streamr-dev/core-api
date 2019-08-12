package com.unifina.service


import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(CommunityService)
@Mock([SecUser, IntegrationKey])
class CommunityServiceSpec extends Specification {
	SecUser me
	IntegrationKey key
	final String adminAddress = "0xCCCC000000000000000000000000AAAA0000FFFF"
	final String communityAddress = "0x0000000000000000000000000000000000000000"

	def setup() {
		service.ethereumService = Mock(EthereumService)
		me = new SecUser(
			name: "First Lastname",
			username: "first@last.com",
			password: "salasana",
		)
		me.id = 1
		me.save(validate: true, failOnError: true)

		IntegrationKey key = new IntegrationKey(
			name: "Key name",
			user: me,
			service: IntegrationKey.Service.ETHEREUM,
			json: "{}",
			idInService: adminAddress,
		)
		key.id = "key-id"
		key.save(validate: true, failOnError: true)
	}

    void "checkAdminAccessControl() test"() {
		when:
		boolean result = service.checkAdminAccessControl(me, communityAddress)
		then:
		1 * service.ethereumService.fetchCommunityAdminsEthereumAddress(communityAddress) >> adminAddress
		result
    }

	void "checkAdminAccessControl() fails on insufficient admin permissions"() {
		when:
		boolean result = service.checkAdminAccessControl(me, communityAddress)
		then:
		1 * service.ethereumService.fetchCommunityAdminsEthereumAddress(communityAddress) >> "0xCCCC000000000000000000000000AAAA00000000"
		!result
	}
}
