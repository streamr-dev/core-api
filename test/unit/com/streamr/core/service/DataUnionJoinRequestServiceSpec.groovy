package com.streamr.core.service

import com.streamr.client.StreamrClient
import com.streamr.client.dataunion.DataUnion
import com.streamr.client.dataunion.DataUnionClient
import com.streamr.core.BeanMockingSpecification
import com.streamr.core.domain.DataUnionJoinRequest
import com.streamr.core.domain.DataUnionSecret
import com.streamr.core.domain.User
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(DataUnionJoinRequestService)
@Mock([User, DataUnionJoinRequest, DataUnionSecret])
class DataUnionJoinRequestServiceSpec extends BeanMockingSpecification {

	private static final String memberAddress = "0xCCCC000000000000000000000000AAAA0000FFFF"
	private static final String contractAddress = "0x0000000000000000000000000000000000000000"
	private static final String privateKey = "e80e35452d97febc624e65614b0a42e8a84603815b3e30f03a3d323062b8c3d2"

	User me
	StreamrClient streamrClientMock
	DataUnionClient dataUnionClientMock
	DataUnion dataUnionMock

	def setup() {
		service.ethereumService = mockBean(EthereumService)
		service.streamrClientService = mockBean(StreamrClientService)

		dataUnionMock = Mock(DataUnion)
		streamrClientMock = Mock(StreamrClient)
		service.streamrClientService.getInstanceForThisEngineNode() >> streamrClientMock
		dataUnionClientMock = Mock(DataUnionClient)

		me = new User(
			name: "First Lastname",
			username: memberAddress,
		)
		me.id = 1
		me.save(validate: true, failOnError: true)

		DataUnionSecret secret = new DataUnionSecret(
			name: "name of the secret",
			secret: "secret",
			contractAddress: contractAddress,
		)
		secret.id = "secret-id"
		secret.save(validate: true, failOnError: true)
	}

	void "create user doesnt have ethereum id"() {
		setup:
		DataUnionJoinRequestCommand cmd = new DataUnionJoinRequestCommand(
			memberAddress: "0xCCCC00000000000000000000AAAAAAAAAAAAAAAA",
		)
		when:
		service.create(contractAddress, cmd, me)

		then:
		def e = thrown(NotFoundException)
		e.statusCode == 404
		e.code == "NOT_FOUND"
	}

	void "create supplied with correct secret"() {
		setup:
		DataUnionJoinRequestCommand cmd = new DataUnionJoinRequestCommand(
			memberAddress: memberAddress,
			secret: "secret",
		)

		when:
		DataUnionJoinRequest c = service.create(contractAddress, cmd, me)

		then:
		1 * streamrClientMock.dataUnionClient(_, _) >> dataUnionClientMock
		1 * dataUnionClientMock.dataUnionFromMainnetAddress(_) >> dataUnionMock
		1 * dataUnionMock.isMemberActive(_) >> true
		c.state == DataUnionJoinRequest.State.ACCEPTED
	}

	void "create supplied without secret"() {
		setup:
		DataUnionJoinRequestCommand cmd = new DataUnionJoinRequestCommand(
			memberAddress: memberAddress,
		)
		when:
		DataUnionJoinRequest c = service.create(contractAddress, cmd, me)

		then:
		c.state == DataUnionJoinRequest.State.PENDING
	}

	void "create supplied with incorrect secret"() {
		setup:
		DataUnionJoinRequestCommand cmd = new DataUnionJoinRequestCommand(
			memberAddress: memberAddress,
			secret: "wrong",
		)

		when:
		service.create(contractAddress, cmd, me)

		then:
		def e = thrown(ApiException)
		e.statusCode == 403
		e.code == "INCORRECT_SECRET"
	}

	void "update rejects accepted state"() {
		setup:
		DataUnionJoinRequest r = new DataUnionJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
			contractAddress: contractAddress,
			user: me,
			state: DataUnionJoinRequest.State.ACCEPTED,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r.save(failOnError: true, validate: true)

		DataUnionUpdateJoinRequestCommand cmd = new DataUnionUpdateJoinRequestCommand(
			state: "ACCEPTED",
		)

		when:
		service.update(contractAddress, r.id, cmd)
		then:
		def e = thrown(ApiException)
		e.statusCode == 400
		e.code == "JOIN_REQUEST_ALREADY_ACCEPTED"
	}

	void "update rejects invalid state"() {
		setup:
		DataUnionJoinRequest r = new DataUnionJoinRequest(
			memberAddress: "0xCCCC000000000000000000000000AAAA0000FFFF",
			contractAddress: contractAddress,
			user: me,
			state: DataUnionJoinRequest.State.ACCEPTED,
			dateCreated: new Date(),
			lastUpdated: new Date(),
		)
		r.save(failOnError: true, validate: true)

		DataUnionUpdateJoinRequestCommand cmd = new DataUnionUpdateJoinRequestCommand(
			state: "NOT_IN_OUR_ENUM",
		)

		when:
		service.update(contractAddress, r.id, cmd)
		then:
		def e = thrown(ApiException)
		e.statusCode == 400
		e.code == "INVALID_JOIN_REQUEST_STATE"
	}
}
