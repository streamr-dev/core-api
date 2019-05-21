package com.unifina.service

import com.unifina.api.UpdateCommunityJoinRequestCommand
import com.unifina.domain.community.CommunityJoinRequest
import com.unifina.domain.security.SecUser
import spock.lang.Specification

class CommunityJoinRequestServiceIntegrationSpec extends Specification {
	CommunityJoinRequestService service = new CommunityJoinRequestService()
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

	void "findAll"() {
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
		List<CommunityJoinRequest> results = service.findAll(communityAddress, null)
		then:
		results.size() == 2
		results.containsAll([r1, r2])
	}

	void "findAll with state"() {
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
		List<CommunityJoinRequest> results = service.findAll(communityAddress, CommunityJoinRequest.State.PENDING)
		then:
		results.size() == 1
		results.get(0).memberAddress == "0xCCCC000000000000000000000000AAAA0000AAAA"
	}

	void "find"() {
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
		CommunityJoinRequest r = service.find(communityAddress, r1.id)
		then:
		r.communityAddress == communityAddress
		r.memberAddress == "0xCCCC000000000000000000000000AAAA0000AAAA"
	}

	void "updateCommunityJoinRequest updates pending state to accepted"() {
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
		def c = service.updateCommunityJoinRequest(communityAddress, r.id, cmd)
		then:
		c.state == CommunityJoinRequest.State.ACCEPTED
		// no changes below
		c.communityAddress == communityAddress
		c.memberAddress == "0xCCCC000000000000000000000000AAAA0000FFFF"
		c.user == me
	}

	void "updateCommunityJoinRequest updates pending state to rejected"() {
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
			state: "REJECTED",
		)

		when:
		def c = service.updateCommunityJoinRequest(communityAddress, r.id, cmd)
		then:
		c.state == CommunityJoinRequest.State.REJECTED
		// no changes below
		c.communityAddress == communityAddress
		c.memberAddress == "0xCCCC000000000000000000000000AAAA0000FFFF"
		c.user == me
	}
}
