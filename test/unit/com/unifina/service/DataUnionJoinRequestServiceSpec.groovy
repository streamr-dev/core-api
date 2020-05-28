package com.unifina.service

import com.streamr.client.StreamrClient
import com.streamr.client.options.StreamrClientOptions
import com.unifina.BeanMockingSpecification
import com.unifina.api.ApiException
import com.unifina.api.DataUnionJoinRequestCommand
import com.unifina.api.NotFoundException
import com.unifina.api.UpdateDataUnionJoinRequestCommand
import com.unifina.domain.dataunion.DataUnionJoinRequest
import com.unifina.domain.dataunion.DataUnionSecret
import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Category
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

@TestFor(DataUnionJoinRequestService)
@Mock([SecUser, IntegrationKey, DataUnionJoinRequest, DataUnionSecret])
class DataUnionJoinRequestServiceSpec extends BeanMockingSpecification {

	private static final String memberAddress = "0xCCCC000000000000000000000000AAAA0000FFFF"
	private static final String contractAddress = "0x0000000000000000000000000000000000000000"

	SecUser me
	StreamrClient streamrClientMock
	com.streamr.client.rest.Stream joinPartStream

	def setup() {
		service.ethereumService = mockBean(EthereumService)
		service.streamrClientService = mockBean(StreamrClientService)
		service.permissionService = mockBean(PermissionService)

		joinPartStream = new com.streamr.client.rest.Stream("join part stream", "")
		joinPartStream.setId("joinPartStream")

		streamrClientMock = Mock(StreamrClient)
		streamrClientMock.getStream(joinPartStream.id) >> joinPartStream
		streamrClientMock.getOptions() >> Mock(StreamrClientOptions)
		service.streamrClientService.getInstanceForThisEngineNode() >> streamrClientMock

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
		1 * service.ethereumService.fetchJoinPartStreamID(contractAddress) >> joinPartStream.id
		1 * streamrClientMock.publish(_, [type: "join", "addresses": [memberAddress]])
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

	void "create sets permissions"() {
		setup:
		SecUser user = new SecUser(
			username: "user@domain.com",
			name: "Firstname Lastname",
			password: "salasana"
		)
		user.id = 1
		user.save(failOnError: true, validate: false)

		DataUnionJoinRequestCommand cmd = new DataUnionJoinRequestCommand(
			memberAddress: memberAddress,
			secret: "secret",
		)
		Stream s1 = new Stream(name: "stream-1")
		Stream s2 = new Stream(name: "stream-2")
		Stream s3 = new Stream(name: "stream-3")
		Stream s4 = new Stream(name: "stream-4")
		[s1, s2, s3, s4].eachWithIndex { Stream stream, int i -> stream.id = "stream-${i+1}" } // assign ids
		[s1, s2, s3, s4]*.save(failOnError: true, validate: false)

		Category category = new Category(name: "Category")
		category.id = "category-id"
		category.save()

		Product product = new Product(
			name: "name",
			description: "description",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: contractAddress,
			streams: [s1, s2, s3, s4],
			pricePerSecond: 10,
			category: category,
			state: Product.State.NOT_DEPLOYED,
			blockNumber: 40000,
			blockIndex: 30,
			owner: user,
			type: Product.Type.DATAUNION,
		)
		product.id = "product-id"
		product.save(failOnError: true, validate: true)

		when:
		DataUnionJoinRequest c = service.create(contractAddress, cmd, me)

		then:
		1 * service.ethereumService.fetchJoinPartStreamID(contractAddress) >> joinPartStream.id
		1 * streamrClientMock.publish(_, [type: "join", "addresses": [memberAddress]])
		1 * service.permissionService.systemGrant(user, s1, Permission.Operation.STREAM_PUBLISH)
		1 * service.permissionService.systemGrant(user, s2, Permission.Operation.STREAM_PUBLISH)
		1 * service.permissionService.systemGrant(user, s3, Permission.Operation.STREAM_PUBLISH)
		1 * service.permissionService.systemGrant(user, s4, Permission.Operation.STREAM_PUBLISH)
		c.state == DataUnionJoinRequest.State.ACCEPTED
	}

	void "create doesn't set permissions if they already exist"() {
		setup:
		SecUser user = new SecUser(
			username: "user@domain.com",
			name: "Firstname Lastname",
			password: "salasana"
		)
		user.id = 1
		user.save(failOnError: true, validate: false)

		DataUnionJoinRequestCommand cmd = new DataUnionJoinRequestCommand(
			memberAddress: memberAddress,
			secret: "secret",
		)
		Stream s1 = new Stream(name: "stream-1")
		Stream s2 = new Stream(name: "stream-2")
		Stream s3 = new Stream(name: "stream-3")
		Stream s4 = new Stream(name: "stream-4")
		[s1, s2, s3, s4].eachWithIndex { Stream stream, int i -> stream.id = "stream-${i+1}" } // assign ids
		[s1, s2, s3, s4]*.save(failOnError: true, validate: false)

		Category category = new Category(name: "Category")
		category.id = "category-id"
		category.save()

		Product product = new Product(
			name: "name",
			description: "description",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: contractAddress,
			streams: [s1, s2, s3, s4],
			pricePerSecond: 10,
			category: category,
			state: Product.State.NOT_DEPLOYED,
			blockNumber: 40000,
			blockIndex: 30,
			owner: user,
			type: Product.Type.DATAUNION,
		)
		product.id = "product-id"
		product.save(failOnError: true, validate: true)

		when:
		DataUnionJoinRequest c = service.create(contractAddress, cmd, me)

		then:
		1 * service.ethereumService.fetchJoinPartStreamID(contractAddress) >> joinPartStream.id
		1 * streamrClientMock.publish(_, [type: "join", "addresses": [memberAddress]])
		1 * service.permissionService.check(user, s1, Permission.Operation.STREAM_PUBLISH) >> true
		1 * service.permissionService.check(user, s2, Permission.Operation.STREAM_PUBLISH) >> true
		1 * service.permissionService.systemGrant(user, s3, Permission.Operation.STREAM_PUBLISH)
		1 * service.permissionService.systemGrant(user, s4, Permission.Operation.STREAM_PUBLISH)
		c.state == DataUnionJoinRequest.State.ACCEPTED
	}

	void "findStreams"() {
		setup:
		SecUser user = new SecUser(
			username: "user@domain.com",
			name: "Firstname Lastname",
			password: "salasana"
		)
		user.id = 1
		user.save(failOnError: true, validate: false)

		Stream s1 = new Stream(name: "stream-1")
		Stream s2 = new Stream(name: "stream-2")
		Stream s3 = new Stream(name: "stream-3")
		Stream s4 = new Stream(name: "stream-4")
		[s1, s2, s3, s4].eachWithIndex { Stream stream, int i -> stream.id = "stream-${i+1}" } // assign ids
		[s1, s2, s3, s4]*.save(failOnError: true, validate: false)

		Category category = new Category(name: "Category")
		category.id = "category-id"
		category.save()

		Product product = new Product(
			name: "name",
			description: "description",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: contractAddress,
			streams: [s1, s2, s3, s1],
			pricePerSecond: 10,
			category: category,
			state: Product.State.NOT_DEPLOYED,
			blockNumber: 40000,
			blockIndex: 30,
			owner: user,
			type: Product.Type.DATAUNION,
		)
		product.id = "product-id"
		product.save(failOnError: true, validate: true)

		when:
		Set<Stream> streams = service.findStreams(new DataUnionJoinRequest(contractAddress:  contractAddress))
		then:
		streams.size() == 3
		streams.contains(s1)
		streams.contains(s2)
		streams.contains(s3)
	}

	void "findMembers"() {
		setup:
		DataUnionJoinRequest c1 = new DataUnionJoinRequest(
			user: new SecUser(),
			memberAddress: "0x0000000000000000000000000000000000000000",
			contractAddress: contractAddress,
		)
		c1.save()
		DataUnionJoinRequest c2 = new DataUnionJoinRequest(
			user: new SecUser(),
			memberAddress: "0x0000000000000000000000000000000000000000",
			contractAddress: contractAddress,
		)
		c2.save()

		when:
		Set<SecUser> members = service.findMembers(contractAddress)

		then:
		members.size() == 2
	}

	void "update sets permissions"() {
		setup:
		SecUser user = new SecUser(
			username: "user@domain.com",
			name: "Firstname Lastname",
			password: "salasana"
		)
		user.id = 1
		user.save(failOnError: true, validate: false)

		Stream s1 = new Stream(name: "stream-1")
		Stream s2 = new Stream(name: "stream-2")
		Stream s3 = new Stream(name: "stream-3")
		Stream s4 = new Stream(name: "stream-4")
		[s1, s2, s3, s4].eachWithIndex { Stream stream, int i -> stream.id = "stream-${i+1}" } // assign ids
		[s1, s2, s3, s4]*.save(failOnError: true, validate: false)

		Category category = new Category(name: "Category")
		category.id = "category-id"
		category.save()

		Product product = new Product(
			name: "name",
			description: "description",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: contractAddress,
			streams: [s1, s2, s3, s4],
			pricePerSecond: 10,
			category: category,
			state: Product.State.NOT_DEPLOYED,
			blockNumber: 40000,
			blockIndex: 30,
			owner: user,
			type: Product.Type.DATAUNION,
		)
		product.id = "product-id"
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

		UpdateDataUnionJoinRequestCommand cmd = new UpdateDataUnionJoinRequestCommand(
			state: "ACCEPTED",
		)

		when:
		service.update(contractAddress, r.id, cmd)
		then:
		1 * service.ethereumService.fetchJoinPartStreamID(contractAddress) >> joinPartStream.id
		1 * streamrClientMock.publish(_, [type: "join", "addresses": [memberAddress]])
		1 * service.permissionService.systemGrant(user, s1, Permission.Operation.STREAM_PUBLISH)
		1 * service.permissionService.systemGrant(user, s2, Permission.Operation.STREAM_PUBLISH)
		1 * service.permissionService.systemGrant(user, s3, Permission.Operation.STREAM_PUBLISH)
		1 * service.permissionService.systemGrant(user, s4, Permission.Operation.STREAM_PUBLISH)
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

		UpdateDataUnionJoinRequestCommand cmd = new UpdateDataUnionJoinRequestCommand(
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

		UpdateDataUnionJoinRequestCommand cmd = new UpdateDataUnionJoinRequestCommand(
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
