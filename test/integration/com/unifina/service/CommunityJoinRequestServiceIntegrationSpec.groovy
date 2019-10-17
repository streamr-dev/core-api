package com.unifina.service

import com.streamr.client.StreamrClient
import com.streamr.client.options.StreamrClientOptions
import com.unifina.api.NotFoundException
import com.unifina.api.UpdateCommunityJoinRequestCommand
import com.unifina.domain.community.CommunityJoinRequest
import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Category
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.ModuleCategory
import spock.lang.Specification

// This is an integration test because Grails doesn't support criteria queries in unit tests
class CommunityJoinRequestServiceIntegrationSpec extends Specification {
	CommunityJoinRequestService service = new CommunityJoinRequestService()
	SecUser me
	com.streamr.client.rest.Stream joinPartStream
	Category category
	final String communityAddress = "0x0000000000000000000000000000000000000000"
	StreamrClient streamrClientMock

	void setup() {
		me = new SecUser(
			name: "First Lastname",
			username: "first@last.com",
			password: "salasana",
		)
		me.save(validate: true, failOnError: true)

		category = new Category(name: "Category")
		category.id = "category-id"
		category.save(validate: true, failOnError: true)
		ModuleCategory mc = new ModuleCategory(name: "module category")
		mc.save(failOnError: true, validate: true)
		Module module = new Module(name: "module name", alternativeNames: "alt names", implementingClass: "x", jsModule: "jsmodule", category: mc, type: "type")
		module.save(failOnError: true, validate: true)

		joinPartStream = new com.streamr.client.rest.Stream("join part stream", "")
		joinPartStream.setId("joinPartStream")

		service.streamrClientService = Mock(StreamrClientService)
		streamrClientMock = Mock(StreamrClient)
		streamrClientMock.getStream(joinPartStream.id) >> joinPartStream
		streamrClientMock.getOptions() >> Mock(StreamrClientOptions)
		service.streamrClientService.getInstanceForThisEngineNode() >> streamrClientMock

		service.ethereumService = Mock(EthereumService)
		service.permissionService = Mock(PermissionService)
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

	void "update updates pending state to accepted"() {
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
		def c = service.update(communityAddress, r.id, cmd)
		then:
		1 * service.ethereumService.fetchJoinPartStreamID(communityAddress) >> joinPartStream.id
		1 * streamrClientMock.publish(_, [type: "join", addresses: [r.memberAddress]])
		c.state == CommunityJoinRequest.State.ACCEPTED
		// no changes below
		c.communityAddress == communityAddress
		c.memberAddress == "0xCCCC000000000000000000000000AAAA0000FFFF"
		c.user == me
	}

	void "update updates pending state to rejected"() {
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
		def c = service.update(communityAddress, r.id, cmd)
		then:
		c.state == CommunityJoinRequest.State.REJECTED
		// no changes below
		c.communityAddress == communityAddress
		c.memberAddress == "0xCCCC000000000000000000000000AAAA0000FFFF"
		c.user == me
	}

	void "delete test"() {
		setup:
		Stream s1 = new Stream(name: "stream-1")
		Stream s2 = new Stream(name: "stream-2")
		Stream s3 = new Stream(name: "stream-3")
		Stream s4 = new Stream(name: "stream-4")
		[s1, s2, s3, s4].eachWithIndex { Stream stream, int i -> stream.id = "stream-${i+1}" } // assign ids
		[s1, s2, s3, s4]*.save(failOnError: true, validate: true)

		Product product = new Product(
			name: "name",
			description: "description",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: communityAddress,
			streams: [s1, s2, s3, s4],
			pricePerSecond: 10,
			category: category,
			state: Product.State.NOT_DEPLOYED,
			blockNumber: 40000,
			blockIndex: 30,
			owner: me,
			type: Product.Type.COMMUNITY,
		)
		product.save(failOnError: true, validate: true)

		CommunityJoinRequest r = new CommunityJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
			communityAddress: communityAddress,
			user: me,
			state: CommunityJoinRequest.State.PENDING,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r.save(failOnError: true, validate: true)
		when:
		service.delete(communityAddress, r.id)
		then:
		1 * service.ethereumService.fetchJoinPartStreamID(communityAddress) >> joinPartStream.id
		1 * streamrClientMock.publish(_, [type: "part", addresses: [r.memberAddress]])
		1 * service.permissionService.systemRevoke(me, s1, Permission.Operation.WRITE)
		1 * service.permissionService.systemRevoke(me, s2, Permission.Operation.WRITE)
		1 * service.permissionService.systemRevoke(me, s3, Permission.Operation.WRITE)
		1 * service.permissionService.systemRevoke(me, s4, Permission.Operation.WRITE)
		CommunityJoinRequest.findById(r.id) == null
	}

	void "delete throws NotFoundException"() {
		when:
		service.delete("not-found", "not-found")
		then:
		def e = thrown(NotFoundException)
		e.statusCode == 404
	}
}
