package com.unifina.service

import com.streamr.client.StreamrClient
import com.streamr.client.options.StreamrClientOptions
import com.unifina.BeanMockingSpecification
import com.unifina.api.ApiException
import com.unifina.api.NotFoundException
import com.unifina.domain.*
import com.unifina.exceptions.JoinRequestException
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonBuilder

@TestFor(DataUnionJoinRequestService)
@Mock([User, IntegrationKey, DataUnionJoinRequest, DataUnionSecret])
class DataUnionJoinRequestServiceSpec extends BeanMockingSpecification {

	private static final String memberAddress = "0xCCCC000000000000000000000000AAAA0000FFFF"
	private static final String contractAddress = "0x0000000000000000000000000000000000000000"

	User me
	StreamrClient streamrClientMock
	com.streamr.client.rest.Stream joinPartStream

	def setup() {
		service.ethereumService = mockBean(EthereumService)
		service.streamrClientService = mockBean(StreamrClientService)
		service.permissionService = mockBean(PermissionService)
		service.dataUnionOperatorService = mockBean(DataUnionOperatorService)

		joinPartStream = new com.streamr.client.rest.Stream("join part stream", "")
		joinPartStream.setId("joinPartStream")

		streamrClientMock = Mock(StreamrClient)
		streamrClientMock.getStream(joinPartStream.id) >> joinPartStream
		streamrClientMock.getOptions() >> Mock(StreamrClientOptions)
		service.streamrClientService.getInstanceForThisEngineNode() >> streamrClientMock

		me = new User(
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

		DataUnionOperatorService.ProxyResponse notFoundStats = new DataUnionOperatorService.ProxyResponse()
		notFoundStats.statusCode = 404
		DataUnionOperatorService.ProxyResponse okStats = new DataUnionOperatorService.ProxyResponse()
		okStats.statusCode = 200
		okStats.body =  new JsonBuilder([
			active: true,
		]).toString()

		when:
		DataUnionJoinRequest c = service.create(contractAddress, cmd, me)

		then:
		1 * service.ethereumService.fetchJoinPartStreamID(contractAddress) >> joinPartStream.id
		2 * service.dataUnionOperatorService.memberStats(contractAddress, memberAddress) >> notFoundStats
		1 * service.dataUnionOperatorService.memberStats(contractAddress, memberAddress) >> okStats
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
		User user = new User(
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

		DataUnionOperatorService.ProxyResponse notFoundStats = new DataUnionOperatorService.ProxyResponse()
		notFoundStats.statusCode = 404
		DataUnionOperatorService.ProxyResponse okStats = new DataUnionOperatorService.ProxyResponse()
		okStats.statusCode = 200
		okStats.body =  new JsonBuilder([
			active: true,
		]).toString()

		when:
		DataUnionJoinRequest c = service.create(contractAddress, cmd, me)

		then:
		1 * service.ethereumService.fetchJoinPartStreamID(contractAddress) >> joinPartStream.id
		2 * service.dataUnionOperatorService.memberStats(contractAddress, memberAddress) >> notFoundStats
		1 * service.dataUnionOperatorService.memberStats(contractAddress, memberAddress) >> okStats
		1 * streamrClientMock.publish(_, [type: "join", "addresses": [memberAddress]])
		1 * service.permissionService.systemGrant(user, s1, Permission.Operation.STREAM_PUBLISH)
		1 * service.permissionService.systemGrant(user, s2, Permission.Operation.STREAM_PUBLISH)
		1 * service.permissionService.systemGrant(user, s3, Permission.Operation.STREAM_PUBLISH)
		1 * service.permissionService.systemGrant(user, s4, Permission.Operation.STREAM_PUBLISH)
		c.state == DataUnionJoinRequest.State.ACCEPTED
	}

	void "create doesn't set permissions if they already exist"() {
		setup:
		User user = new User(
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

		DataUnionOperatorService.ProxyResponse notFoundStats = new DataUnionOperatorService.ProxyResponse()
		notFoundStats.statusCode = 404
		DataUnionOperatorService.ProxyResponse okStats = new DataUnionOperatorService.ProxyResponse()
		okStats.statusCode = 200
		okStats.body =  new JsonBuilder([
			active: true,
		]).toString()

		when:
		DataUnionJoinRequest c = service.create(contractAddress, cmd, me)

		then:
		1 * service.ethereumService.fetchJoinPartStreamID(contractAddress) >> joinPartStream.id
		2 * service.dataUnionOperatorService.memberStats(contractAddress, memberAddress) >> notFoundStats
		1 * service.dataUnionOperatorService.memberStats(contractAddress, memberAddress) >> okStats
		1 * streamrClientMock.publish(_, [type: "join", "addresses": [memberAddress]])
		1 * service.permissionService.check(user, s1, Permission.Operation.STREAM_PUBLISH) >> true
		1 * service.permissionService.check(user, s2, Permission.Operation.STREAM_PUBLISH) >> true
		1 * service.permissionService.systemGrant(user, s3, Permission.Operation.STREAM_PUBLISH)
		1 * service.permissionService.systemGrant(user, s4, Permission.Operation.STREAM_PUBLISH)
		c.state == DataUnionJoinRequest.State.ACCEPTED
	}

	void "findStreams"() {
		setup:
		User user = new User(
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
			user: new User(),
			memberAddress: "0x0000000000000000000000000000000000000000",
			contractAddress: contractAddress,
		)
		c1.save()
		DataUnionJoinRequest c2 = new DataUnionJoinRequest(
			user: new User(),
			memberAddress: "0x0000000000000000000000000000000000000000",
			contractAddress: contractAddress,
		)
		c2.save()

		when:
		Set<User> members = service.findMembers(contractAddress)

		then:
		members.size() == 2
	}

	void "update doesnt send join request if already joined"() {
		setup:
		User user = new User(
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

		DataUnionOperatorService.ProxyResponse stats = new DataUnionOperatorService.ProxyResponse()
		stats.statusCode = 200
		stats.body = new JsonBuilder([
			active: true,
		]).toString()

		when:
		service.update(contractAddress, r.id, cmd)
		then:
		1 * service.dataUnionOperatorService.memberStats(contractAddress, memberAddress) >> stats
		0 * service.ethereumService._
		0 * streamrClientMock._
		0 * service.permissionService._
	}

	void "update handles memberStats error"() {
		setup:
		User user = new User(
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

		DataUnionOperatorService.ProxyResponse okStats = new DataUnionOperatorService.ProxyResponse()
		okStats.statusCode = 200
		okStats.body =  new JsonBuilder([
			active: true,
		]).toString()

		when:
		service.update(contractAddress, r.id, cmd)
		then:
		1 * service.ethereumService.fetchJoinPartStreamID(contractAddress) >> joinPartStream.id
		1 * service.dataUnionOperatorService.memberStats(contractAddress, memberAddress) >>  {
			throw new DataUnionProxyException("mocked exception")
		}
		1 * service.dataUnionOperatorService.memberStats(contractAddress, memberAddress) >> okStats
		1 * streamrClientMock.publish(_, [type: "join", "addresses": [memberAddress]])
		1 * service.permissionService.systemGrant(user, s1, Permission.Operation.STREAM_PUBLISH)
		1 * service.permissionService.systemGrant(user, s2, Permission.Operation.STREAM_PUBLISH)
		1 * service.permissionService.systemGrant(user, s3, Permission.Operation.STREAM_PUBLISH)
		1 * service.permissionService.systemGrant(user, s4, Permission.Operation.STREAM_PUBLISH)
	}

	void "update fails if data union server doesnt register join"() {
		setup:
		User user = new User(
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

		DataUnionOperatorService.ProxyResponse notFoundStats = new DataUnionOperatorService.ProxyResponse()
		notFoundStats.statusCode = 404

		when:
		service.update(contractAddress, r.id, cmd)
		then:
		1 * service.ethereumService.fetchJoinPartStreamID(contractAddress) >> joinPartStream.id
		11 * service.dataUnionOperatorService.memberStats(contractAddress, memberAddress) >> notFoundStats
		1 * streamrClientMock.publish(_, [type: "join", "addresses": [memberAddress]])
		1 * service.permissionService.systemGrant(user, s1, Permission.Operation.STREAM_PUBLISH)
		1 * service.permissionService.systemGrant(user, s2, Permission.Operation.STREAM_PUBLISH)
		1 * service.permissionService.systemGrant(user, s3, Permission.Operation.STREAM_PUBLISH)
		1 * service.permissionService.systemGrant(user, s4, Permission.Operation.STREAM_PUBLISH)
		thrown(JoinRequestException)
	}

	void "update sets permissions"() {
		setup:
		User user = new User(
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

		DataUnionOperatorService.ProxyResponse notFoundStats = new DataUnionOperatorService.ProxyResponse()
		notFoundStats.statusCode = 404
		DataUnionOperatorService.ProxyResponse okStats = new DataUnionOperatorService.ProxyResponse()
		okStats.statusCode = 200
		okStats.body =  new JsonBuilder([
			active: true,
		]).toString()

		when:
		service.update(contractAddress, r.id, cmd)
		then:
		1 * service.ethereumService.fetchJoinPartStreamID(contractAddress) >> joinPartStream.id
		2 * service.dataUnionOperatorService.memberStats(contractAddress, memberAddress) >> notFoundStats
		1 * service.dataUnionOperatorService.memberStats(contractAddress, memberAddress) >> okStats
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
