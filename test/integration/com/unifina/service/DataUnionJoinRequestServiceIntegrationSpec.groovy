package com.unifina.service

import com.streamr.client.StreamrClient
import com.streamr.client.dataunion.DataUnion
import com.streamr.client.dataunion.DataUnionClient
import com.streamr.client.options.StreamrClientOptions
import com.unifina.domain.Category
import com.unifina.domain.DataUnionJoinRequest
import com.unifina.domain.Product
import com.unifina.domain.User
import spock.lang.Specification

// This is an integration test because Grails doesn't support criteria queries in unit tests
class DataUnionJoinRequestServiceIntegrationSpec extends Specification {
	DataUnionJoinRequestService service = new DataUnionJoinRequestService()
	User me
	Category category
	final String contractAddress = "0x0000000000000000000000000000000000000000"
	StreamrClient streamrClientMock
	DataUnionClient dataUnionClientMock
	DataUnion dataUnionMock

	void setup() {
		me = new User(
			name: "First Lastname",
			username: "0x0000000000000000000000000000000000000001",
		)
		me.save(validate: true, failOnError: true)

		category = new Category(name: "Category")
		category.id = "category-id"
		category.save(validate: true, failOnError: true)

		service.streamrClientService = Mock(StreamrClientService)
		streamrClientMock = Mock(StreamrClient)
		streamrClientMock.getOptions() >> Mock(StreamrClientOptions)
		service.streamrClientService.getInstanceForThisEngineNode() >> streamrClientMock
		dataUnionClientMock = Mock(DataUnionClient)
		dataUnionMock = Mock(DataUnion)

		Product product = new Product(
			beneficiaryAddress: contractAddress,
			owner: me
		)
		product.save(failOnError: true, validate: false)

		service.ethereumService = Mock(EthereumService)
	}

	void "findAll"() {
		setup:
		DataUnionJoinRequest r1 = new DataUnionJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000AAAA",
			contractAddress: contractAddress,
			user: me,
			state: DataUnionJoinRequest.State.PENDING,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r1.save(failOnError: true, validate: true)
		DataUnionJoinRequest r2 = new DataUnionJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
			contractAddress: contractAddress,
			user: me,
			state: DataUnionJoinRequest.State.ACCEPTED,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r2.save(failOnError: true, validate: true)
		DataUnionJoinRequest r3 = new DataUnionJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000CCCC",
			contractAddress: "0x000000FF00000FF000FF00000FF00000FF0000FF",
			user: me,
			state: DataUnionJoinRequest.State.REJECTED,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r3.save(failOnError: true, validate: true)
		when:
		List<DataUnionJoinRequest> results = service.findAll(contractAddress, null)
		then:
		results.size() == 2
		results.containsAll([r1, r2])
	}

	void "findAll with state"() {
		setup:
		DataUnionJoinRequest r1 = new DataUnionJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000AAAA",
			contractAddress: contractAddress,
			user: me,
			state: DataUnionJoinRequest.State.PENDING,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r1.save(failOnError: true, validate: true)
		DataUnionJoinRequest r2 = new DataUnionJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
			contractAddress: contractAddress,
			user: me,
			state: DataUnionJoinRequest.State.ACCEPTED,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r2.save(failOnError: true, validate: true)
		DataUnionJoinRequest r3 = new DataUnionJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000CCCC",
			contractAddress: "0x0000000000000000000000000000000000000000",
			user: me,
			state: DataUnionJoinRequest.State.REJECTED,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r3.save(failOnError: true, validate: true)
		when:
		List<DataUnionJoinRequest> results = service.findAll(contractAddress, DataUnionJoinRequest.State.PENDING)
		then:
		results.size() == 1
		results.get(0).memberAddress == "0xCCCC000000000000000000000000AAAA0000AAAA"
	}

	void "find"() {
		setup:
		DataUnionJoinRequest r1 = new DataUnionJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000AAAA",
			contractAddress: contractAddress,
			user: me,
			state: DataUnionJoinRequest.State.PENDING,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r1.save(failOnError: true, validate: true)
		DataUnionJoinRequest r2 = new DataUnionJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
			contractAddress: contractAddress,
			user: me,
			state: DataUnionJoinRequest.State.PENDING,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r2.save(failOnError: true, validate: true)
		when:
		DataUnionJoinRequest r = service.find(contractAddress, r1.id)
		then:
		r.contractAddress == contractAddress
		r.memberAddress == "0xCCCC000000000000000000000000AAAA0000AAAA"
	}

	void "update updates pending state to accepted"() {
		setup:
		String memberAddress = "0xCCCC000000000000000000000000AAAA0000FFFF"
		DataUnionJoinRequest r = new DataUnionJoinRequest(
			memberAddress: memberAddress,
			contractAddress: contractAddress,
			user: me,
			state: DataUnionJoinRequest.State.PENDING,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r.save(failOnError: true, validate: true)

		DataUnionUpdateJoinRequestCommand cmd = new DataUnionUpdateJoinRequestCommand(
			state: "ACCEPTED",
		)

		when:
		def c = service.update(contractAddress, r.id, cmd)
		then:
		1 * streamrClientMock.dataUnionClient(_, _) >> dataUnionClientMock
		1 * dataUnionClientMock.dataUnionFromMainnetAddress(_) >> dataUnionMock
		1 * dataUnionMock.isMemberActive(_) >> true
		c.state == DataUnionJoinRequest.State.ACCEPTED
		// no changes below
		c.contractAddress == contractAddress
		c.memberAddress == "0xCCCC000000000000000000000000AAAA0000FFFF"
		c.user == me
	}

	void "update updates pending state to rejected"() {
		setup:
		DataUnionJoinRequest r = new DataUnionJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
			contractAddress: contractAddress,
			user: me,
			state: DataUnionJoinRequest.State.PENDING,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r.save(failOnError: true, validate: true)

		DataUnionUpdateJoinRequestCommand cmd = new DataUnionUpdateJoinRequestCommand(
			state: "REJECTED",
		)

		when:
		def c = service.update(contractAddress, r.id, cmd)
		then:
		c.state == DataUnionJoinRequest.State.REJECTED
		// no changes below
		c.contractAddress == contractAddress
		c.memberAddress == "0xCCCC000000000000000000000000AAAA0000FFFF"
		c.user == me
	}

	void "delete test"() {
		setup:
		final String contractAddress = "0x0000000000000000000000000000000000000001"

		Product product = new Product(
			name: "name",
			description: "description",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: contractAddress,
			streams: [
				"0x0000000000000000000000000000000000000001",
				"0x0000000000000000000000000000000000000002",
				"0x0000000000000000000000000000000000000003",
				"0x0000000000000000000000000000000000000004",
			],
			pricePerSecond: 10,
			category: category,
			state: Product.State.NOT_DEPLOYED,
			blockNumber: 40000,
			blockIndex: 30,
			owner: me,
			type: Product.Type.DATAUNION,
		)
		product.save(failOnError: true, validate: true)

		DataUnionJoinRequest r = new DataUnionJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
			contractAddress: contractAddress,
			user: me,
			state: DataUnionJoinRequest.State.PENDING,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r.save(failOnError: true, validate: true)
		when:
		service.delete(contractAddress, r.id)
		then:
		DataUnionJoinRequest.findById(r.id) == null
	}

	void "delete throws NotFoundException"() {
		when:
		service.delete("not-found", "not-found")
		then:
		def e = thrown(NotFoundException)
		e.statusCode == 404
	}
}
