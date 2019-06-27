package com.unifina.service

import com.unifina.api.ApiException
import com.unifina.api.CommunityJoinRequestCommand
import com.unifina.api.NotFoundException
import com.unifina.domain.community.CommunityJoinRequest
import com.unifina.domain.community.CommunitySecret
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification

@TestFor(CommunityProductService)
@Mock([SecUser, IntegrationKey, CommunitySecret, CommunityJoinRequest])
class CommunityProductServiceSpec extends Specification {
	SecUser me
	final String memberAddress = "0xCCCC000000000000000000000000AAAA0000FFFF"
	final String communityAddress = "0x0000000000000000000000000000000000000000"

	def setup() {
		service.encoder = Mock(PasswordEncoder)

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
			idInService: memberAddress,
		)
		key.id = "key-id"
		key.save(validate: true, failOnError: true)

		CommunitySecret secret = new CommunitySecret(
			name: "name of the community secret",
			secret: "secret",
			communityAddress: communityAddress,
		)
		secret.id = "secret-id"
		secret.save(validate: true, failOnError: true)
    }

	void "createCommunityJoinRequest user doesnt have ethereum id"() {
		setup:
		CommunityJoinRequestCommand cmd = new CommunityJoinRequestCommand(
			memberAddress: "0xCCCC00000000000000000000AAAAAAAAAAAAAAAA",
		)
		when:
		service.createCommunityJoinRequest(communityAddress, cmd, me)

		then:
		def e = thrown(NotFoundException)
		e.statusCode == 404
		e.code == "NOT_FOUND"
	}

    void "createCommunityJoinRequest supplied with correct community secret"() {
		setup:
		CommunityJoinRequestCommand cmd = new CommunityJoinRequestCommand(
			memberAddress: memberAddress,
			secret: "secret",
		)
		when:
		1 * service.encoder.matches("secret", "secret") >> true
		CommunityJoinRequest c = service.createCommunityJoinRequest(communityAddress, cmd, me)

		then:
		c.state == CommunityJoinRequest.State.ACCEPTED
    }

	void "createCommunityJoinRequest supplied without community secret"() {
		setup:
		CommunityJoinRequestCommand cmd = new CommunityJoinRequestCommand(
			memberAddress: memberAddress,
		)
		when:
		0 * service.encoder._
		CommunityJoinRequest c = service.createCommunityJoinRequest(communityAddress, cmd, me)

		then:
		c.state == CommunityJoinRequest.State.PENDING
	}

	void "createCommunityJoinRequest supplied with incorrect community secret"() {
		setup:
		CommunityJoinRequestCommand cmd = new CommunityJoinRequestCommand(
			memberAddress: memberAddress,
			secret: "wrong",
		)

		when:
		1 * service.encoder.matches("wrong", "secret") >> false
		service.createCommunityJoinRequest(communityAddress, cmd, me)

		then:
		def e = thrown(ApiException)
		e.statusCode == 403
		e.code == "INCORRECT_COMMUNITY_SECRET"
	}
}
