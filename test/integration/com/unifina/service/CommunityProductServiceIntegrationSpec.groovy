package com.unifina.service

import com.unifina.api.CommunityJoinRequestCommand
import com.unifina.api.UpdateCommunityJoinRequestCommand
import com.unifina.domain.community.CommunityJoinRequest
import com.unifina.domain.security.SecUser
import spock.lang.Specification

class CommunityProductServiceIntegrationSpec extends Specification {
	CommunityProductService service = new CommunityProductService()
	SecUser me
	final String communityAddress = "0x0000000000000000000000000000000000000000"

	void setup() {
		me = new SecUser(
			name: "First Lastname",
			username: "first@last.com",
			password: "salasana",
		)
		me.save(validate: true, failOnError: true)
	}

	void "findCommunityJoinRequests"() {
		setup:
		CommunityJoinRequest r1 = new CommunityJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000AAAA",
			communityAddress: communityAddress,
			user: me,
			state: CommunityJoinRequest.State.PENDING,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r1.save(failOnError: true, validate: true)
		CommunityJoinRequest r2 = new CommunityJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
			communityAddress: communityAddress,
			user: me,
			state: CommunityJoinRequest.State.ACCEPTED,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r2.save(failOnError: true, validate: true)
		CommunityJoinRequest r3 = new CommunityJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000CCCC",
			communityAddress: "0x000000FF00000FF000FF00000FF00000FF0000FF",
			user: me,
			state: CommunityJoinRequest.State.REJECTED,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r3.save(failOnError: true, validate: true)
		when:
		List<CommunityJoinRequest> results = service.findCommunityJoinRequests(communityAddress, null)
		then:
		results.size() == 2
		results.containsAll([r1, r2])
	}

	void "findCommunityJoinRequests with state"() {
		setup:
		CommunityJoinRequest r1 = new CommunityJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000AAAA",
			communityAddress: communityAddress,
			user: me,
			state: CommunityJoinRequest.State.PENDING,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r1.save(failOnError: true, validate: true)
		CommunityJoinRequest r2 = new CommunityJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
			communityAddress: communityAddress,
			user: me,
			state: CommunityJoinRequest.State.ACCEPTED,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r2.save(failOnError: true, validate: true)
		CommunityJoinRequest r3 = new CommunityJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000CCCC",
			communityAddress: "0x0000000000000000000000000000000000000000",
			user: me,
			state: CommunityJoinRequest.State.REJECTED,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r3.save(failOnError: true, validate: true)
		when:
		List<CommunityJoinRequest> results = service.findCommunityJoinRequests(communityAddress, CommunityJoinRequest.State.PENDING)
		then:
		results.size() == 1
		results.get(0).memberAddress == "0xCCCC000000000000000000000000AAAA0000AAAA"
	}

	void "findCommunityJoinRequest"() {
		setup:
		CommunityJoinRequest r1 = new CommunityJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000AAAA",
			communityAddress: communityAddress,
			user: me,
			state: CommunityJoinRequest.State.PENDING,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r1.save(failOnError: true, validate: true)
		CommunityJoinRequest r2 = new CommunityJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
			communityAddress: communityAddress,
			user: me,
			state: CommunityJoinRequest.State.PENDING,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r2.save(failOnError: true, validate: true)
		when:
		CommunityJoinRequest r = service.findCommunityJoinRequest(communityAddress, r1.id)
		then:
		r.communityAddress == communityAddress
		r.memberAddress == "0xCCCC000000000000000000000000AAAA0000AAAA"
	}

	void "updateCommunityJoinRequest accepts request"() {
		setup:
		CommunityJoinRequest r = new CommunityJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
			communityAddress: communityAddress,
			user: me,
			state: CommunityJoinRequest.State.PENDING,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r.save(failOnError: true, validate: true)

		UpdateCommunityJoinRequestCommand cmd = new UpdateCommunityJoinRequestCommand(
			state: "ACCEPTED",
		)

		when:
		service.updateCommunityJoinRequest(communityAddress, r.id, cmd)

		def c = CommunityJoinRequest.findById(r.id)
		then:
		c.state == CommunityJoinRequest.State.ACCEPTED
		// no changes below
		c.communityAddress == communityAddress
		c.memberAddress == "0xCCCC000000000000000000000000AAAA0000FFFF"
		c.user == me
	}
}
