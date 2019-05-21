package com.unifina.service

import com.unifina.api.ApiException
import com.unifina.api.CommunityJoinRequestCommand
import com.unifina.api.NotFoundException
import com.unifina.api.UpdateCommunityJoinRequestCommand
import com.unifina.domain.community.CommunityJoinRequest
import com.unifina.domain.community.CommunitySecret
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(CommunityJoinRequestService)
@Mock([SecUser, IntegrationKey, CommunitySecret, CommunityJoinRequest])
class CommunityJoinRequestServiceSpec extends Specification {
	SecUser me
	final String memberAddress = "0xCCCC000000000000000000000000AAAA0000FFFF"
	final String communityAddress = "0x0000000000000000000000000000000000000000"

	def setup() {
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

	void "create user doesnt have ethereum id"() {
		setup:
		CommunityJoinRequestCommand cmd = new CommunityJoinRequestCommand(
			memberAddress: "0xCCCC00000000000000000000AAAAAAAAAAAAAAAA",
		)
		when:
		service.create(communityAddress, cmd, me)

		then:
		def e = thrown(NotFoundException)
		e.statusCode == 404
		e.code == "NOT_FOUND"
	}

    void "create supplied with correct community secret"() {
		setup:
		CommunityJoinRequestCommand cmd = new CommunityJoinRequestCommand(
			memberAddress: memberAddress,
			secret: "secret",
		)
		when:
		CommunityJoinRequest c = service.create(communityAddress, cmd, me)

		then:
		c.state == CommunityJoinRequest.State.ACCEPTED
    }

	void "create supplied without community secret"() {
		setup:
		CommunityJoinRequestCommand cmd = new CommunityJoinRequestCommand(
			memberAddress: memberAddress,
		)
		when:
		CommunityJoinRequest c = service.create(communityAddress, cmd, me)

		then:
		c.state == CommunityJoinRequest.State.PENDING
	}

	void "create supplied with incorrect community secret"() {
		setup:
		CommunityJoinRequestCommand cmd = new CommunityJoinRequestCommand(
			memberAddress: memberAddress,
			secret: "wrong",
		)

		when:
		service.create(communityAddress, cmd, me)

		then:
		def e = thrown(ApiException)
		e.statusCode == 403
		e.code == "INCORRECT_COMMUNITY_SECRET"
	}

	void "updateCommunityJoinRequest rejects accepted state"() {
		setup:
		CommunityJoinRequest r = new CommunityJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
			communityAddress: communityAddress,
			user: me,
			state: CommunityJoinRequest.State.ACCEPTED,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r.save(failOnError: true, validate: true)

		UpdateCommunityJoinRequestCommand cmd = new UpdateCommunityJoinRequestCommand(
			state: "ACCEPTED",
		)

		when:
		service.updateCommunityJoinRequest(communityAddress, r.id, cmd)
		then:
		def e = thrown(ApiException)
		e.statusCode == 400
		e.code == "JOIN_REQUEST_ALREADY_ACCEPTED"
	}

	void "updateCommunityJoinRequest rejects invalid state"() {
		setup:
		CommunityJoinRequest r = new CommunityJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
			communityAddress: communityAddress,
			user: me,
			state: CommunityJoinRequest.State.ACCEPTED,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r.save(failOnError: true, validate: true)

		UpdateCommunityJoinRequestCommand cmd = new UpdateCommunityJoinRequestCommand(
			state: "NOT_IN_OUR_ENUM",
		)

		when:
		service.updateCommunityJoinRequest(communityAddress, r.id, cmd)
		then:
		def e = thrown(ApiException)
		e.statusCode == 400
		e.code == "INVALID_JOIN_REQUEST_STATE"
	}
}
