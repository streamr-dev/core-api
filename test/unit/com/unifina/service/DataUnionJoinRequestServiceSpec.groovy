package com.unifina.service

import com.streamr.client.StreamrClient
import com.streamr.client.dataunion.DataUnion
import com.streamr.client.dataunion.DataUnionClient
import com.streamr.client.dataunion.EthereumTransactionReceipt
import com.streamr.client.options.StreamrClientOptions
import com.unifina.BeanMockingSpecification
import com.unifina.domain.*
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import groovy.json.JsonBuilder

@TestFor(DataUnionJoinRequestService)
@Mock([User, DataUnionJoinRequest, DataUnionSecret])
class DataUnionJoinRequestServiceSpec extends BeanMockingSpecification {

	private static final String memberAddress = "0xCCCC000000000000000000000000AAAA0000FFFF"
	private static final String contractAddress = "0x0000000000000000000000000000000000000000"

	User me
	StreamrClient streamrClientMock
	com.streamr.client.rest.Stream joinPartStream
	ProductService productService

	def setup() {
		service.ethereumService = mockBean(EthereumService)
		service.streamrClientService = mockBean(StreamrClientService)
		service.permissionService = mockBean(PermissionService)
		service.dataUnionService = mockBean(DataUnionService)

		joinPartStream = new com.streamr.client.rest.Stream("join part stream", "")
		joinPartStream.setId("joinPartStream")

		streamrClientMock = Mock(StreamrClient)
		streamrClientMock.getStream(joinPartStream.id) >> joinPartStream
		streamrClientMock.getOptions() >> Mock(StreamrClientOptions)
		service.streamrClientService.getInstanceForThisEngineNode() >> streamrClientMock

		Product mockProduct = new Product()
		mockProduct.dataUnionVersion = 1
		productService = mockBean(ProductService)
		productService.findByBeneficiaryAddress(_) >> mockProduct

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

		DataUnionService.ProxyResponse notFoundStats = new DataUnionService.ProxyResponse()
		notFoundStats.statusCode = 404
		DataUnionService.ProxyResponse okStats = new DataUnionService.ProxyResponse()
		okStats.statusCode = 200
		okStats.body = new JsonBuilder([
			active: true,
		]).toString()

		when:
		DataUnionJoinRequest c = service.create(contractAddress, cmd, me)

		then:
		1 * service.ethereumService.fetchJoinPartStreamID(contractAddress) >> joinPartStream.id
		2 * service.dataUnionService.memberStats(contractAddress, memberAddress) >> notFoundStats
		1 * service.dataUnionService.memberStats(contractAddress, memberAddress) >> okStats
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
		[s1, s2, s3, s4].eachWithIndex { Stream stream, int i -> stream.id = "stream-${i + 1}" } // assign ids
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

		DataUnionService.ProxyResponse notFoundStats = new DataUnionService.ProxyResponse()
		notFoundStats.statusCode = 404
		DataUnionService.ProxyResponse okStats = new DataUnionService.ProxyResponse()
		okStats.statusCode = 200
		okStats.body = new JsonBuilder([
			active: true,
		]).toString()

		when:
		DataUnionJoinRequest c = service.create(contractAddress, cmd, me)

		then:
		1 * service.ethereumService.fetchJoinPartStreamID(contractAddress) >> joinPartStream.id
		2 * service.dataUnionService.memberStats(contractAddress, memberAddress) >> notFoundStats
		1 * service.dataUnionService.memberStats(contractAddress, memberAddress) >> okStats
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
		[s1, s2, s3, s4].eachWithIndex { Stream stream, int i -> stream.id = "stream-${i + 1}" } // assign ids
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

		DataUnionService.ProxyResponse notFoundStats = new DataUnionService.ProxyResponse()
		notFoundStats.statusCode = 404
		DataUnionService.ProxyResponse okStats = new DataUnionService.ProxyResponse()
		okStats.statusCode = 200
		okStats.body = new JsonBuilder([
			active: true,
		]).toString()

		when:
		DataUnionJoinRequest c = service.create(contractAddress, cmd, me)

		then:
		1 * service.ethereumService.fetchJoinPartStreamID(contractAddress) >> joinPartStream.id
		2 * service.dataUnionService.memberStats(contractAddress, memberAddress) >> notFoundStats
		1 * service.dataUnionService.memberStats(contractAddress, memberAddress) >> okStats
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
		)
		user.id = 1
		user.save(failOnError: true, validate: false)

		Stream s1 = new Stream(name: "stream-1")
		Stream s2 = new Stream(name: "stream-2")
		Stream s3 = new Stream(name: "stream-3")
		Stream s4 = new Stream(name: "stream-4")
		[s1, s2, s3, s4].eachWithIndex { Stream stream, int i -> stream.id = "stream-${i + 1}" } // assign ids
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
		Set<Stream> streams = service.findStreams(new DataUnionJoinRequest(contractAddress: contractAddress))
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
		)
		user.id = 1
		user.save(failOnError: true, validate: false)

		Stream s1 = new Stream(name: "stream-1")
		Stream s2 = new Stream(name: "stream-2")
		Stream s3 = new Stream(name: "stream-3")
		Stream s4 = new Stream(name: "stream-4")
		[s1, s2, s3, s4].eachWithIndex { Stream stream, int i -> stream.id = "stream-${i + 1}" } // assign ids
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

		DataUnionUpdateJoinRequestCommand cmd = new DataUnionUpdateJoinRequestCommand(
			state: "ACCEPTED",
		)

		DataUnionService.ProxyResponse stats = new DataUnionService.ProxyResponse()
		stats.statusCode = 200
		stats.body = new JsonBuilder([
			active: true,
		]).toString()

		when:
		service.update(contractAddress, r.id, cmd)
		then:
		1 * service.dataUnionService.memberStats(contractAddress, memberAddress) >> stats
		0 * service.ethereumService._
		0 * streamrClientMock._
		0 * service.permissionService._
	}

	void "update handles memberStats error"() {
		setup:
		User user = new User(
			username: "user@domain.com",
			name: "Firstname Lastname",
		)
		user.id = 1
		user.save(failOnError: true, validate: false)

		Stream s1 = new Stream(name: "stream-1")
		Stream s2 = new Stream(name: "stream-2")
		Stream s3 = new Stream(name: "stream-3")
		Stream s4 = new Stream(name: "stream-4")
		[s1, s2, s3, s4].eachWithIndex { Stream stream, int i -> stream.id = "stream-${i + 1}" } // assign ids
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

		DataUnionUpdateJoinRequestCommand cmd = new DataUnionUpdateJoinRequestCommand(
			state: "ACCEPTED",
		)

		DataUnionService.ProxyResponse okStats = new DataUnionService.ProxyResponse()
		okStats.statusCode = 200
		okStats.body = new JsonBuilder([
			active: true,
		]).toString()

		when:
		service.update(contractAddress, r.id, cmd)
		then:
		1 * service.ethereumService.fetchJoinPartStreamID(contractAddress) >> joinPartStream.id
		1 * service.dataUnionService.memberStats(contractAddress, memberAddress) >> {
			throw new DataUnionProxyException("mocked exception")
		}
		1 * service.dataUnionService.memberStats(contractAddress, memberAddress) >> okStats
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
		)
		user.id = 1
		user.save(failOnError: true, validate: false)

		Stream s1 = new Stream(name: "stream-1")
		Stream s2 = new Stream(name: "stream-2")
		Stream s3 = new Stream(name: "stream-3")
		Stream s4 = new Stream(name: "stream-4")
		[s1, s2, s3, s4].eachWithIndex { Stream stream, int i -> stream.id = "stream-${i + 1}" } // assign ids
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

		DataUnionUpdateJoinRequestCommand cmd = new DataUnionUpdateJoinRequestCommand(
			state: "ACCEPTED",
		)

		DataUnionService.ProxyResponse notFoundStats = new DataUnionService.ProxyResponse()
		notFoundStats.statusCode = 404

		when:
		service.update(contractAddress, r.id, cmd)
		then:
		1 * service.ethereumService.fetchJoinPartStreamID(contractAddress) >> joinPartStream.id
		11 * service.dataUnionService.memberStats(contractAddress, memberAddress) >> notFoundStats
		1 * streamrClientMock.publish(_, [type: "join", "addresses": [memberAddress]])
		1 * service.permissionService.systemGrant(user, s1, Permission.Operation.STREAM_PUBLISH)
		1 * service.permissionService.systemGrant(user, s2, Permission.Operation.STREAM_PUBLISH)
		1 * service.permissionService.systemGrant(user, s3, Permission.Operation.STREAM_PUBLISH)
		1 * service.permissionService.systemGrant(user, s4, Permission.Operation.STREAM_PUBLISH)
		thrown(DataUnionJoinRequestException)
	}

	void "accepting joinRequest grants permissions to streams in product"() {
		setup:
		User user = new User(
			username: "user@domain.com",
			name: "Firstname Lastname",
		)
		user.id = 1
		user.save(failOnError: true, validate: false)

		Stream s1 = new Stream(name: "stream-1")
		Stream s2 = new Stream(name: "stream-2")
		Stream s3 = new Stream(name: "stream-3")
		Stream s4 = new Stream(name: "stream-4")
		[s1, s2, s3, s4].eachWithIndex { Stream stream, int i -> stream.id = "stream-${i + 1}" } // assign ids
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

		DataUnionUpdateJoinRequestCommand cmd = new DataUnionUpdateJoinRequestCommand(
			state: "ACCEPTED",
		)

		DataUnionService.ProxyResponse notFoundStats = new DataUnionService.ProxyResponse()
		notFoundStats.statusCode = 404
		DataUnionService.ProxyResponse okStats = new DataUnionService.ProxyResponse()
		okStats.statusCode = 200
		okStats.body = new JsonBuilder([
			active: true,
		]).toString()

		when:
		service.update(contractAddress, r.id, cmd)
		then:
		1 * service.ethereumService.fetchJoinPartStreamID(contractAddress) >> joinPartStream.id
		2 * service.dataUnionService.memberStats(contractAddress, memberAddress) >> notFoundStats
		1 * service.dataUnionService.memberStats(contractAddress, memberAddress) >> okStats
		1 * streamrClientMock.publish(_, [type: "join", "addresses": [memberAddress]])
		1 * service.permissionService.systemGrant(user, s1, Permission.Operation.STREAM_PUBLISH)
		1 * service.permissionService.systemGrant(user, s2, Permission.Operation.STREAM_PUBLISH)
		1 * service.permissionService.systemGrant(user, s3, Permission.Operation.STREAM_PUBLISH)
		1 * service.permissionService.systemGrant(user, s4, Permission.Operation.STREAM_PUBLISH)
	}

	void "accepting joinRequest grants permissions to streams in product (DU2)"() {
		setup:
		User user = new User(
			username: "user@domain.com",
			name: "Firstname Lastname",
		)
		user.id = 1
		user.save(failOnError: true, validate: false)

		Stream s1 = new Stream(name: "stream-1")
		Stream s2 = new Stream(name: "stream-2")
		Stream s3 = new Stream(name: "stream-3")
		Stream s4 = new Stream(name: "stream-4")
		[s1, s2, s3, s4].eachWithIndex { Stream stream, int i -> stream.id = "stream-${i + 1}" } // assign ids
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
			dataUnionVersion: 2
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

		DataUnionUpdateJoinRequestCommand cmd = new DataUnionUpdateJoinRequestCommand(
			state: "ACCEPTED",
		)

		DataUnionService.ProxyResponse notFoundStats = new DataUnionService.ProxyResponse()
		notFoundStats.statusCode = 404
		DataUnionService.ProxyResponse okStats = new DataUnionService.ProxyResponse()
		okStats.statusCode = 200
		okStats.body = new JsonBuilder([
			active: true,
		]).toString()

		boolean memberActive = false
		DataUnionClient duClient = Mock(DataUnionClient)
		DataUnion du = Mock(DataUnion)
		duClient.dataUnionFromMainnetAddress(contractAddress) >> du
		duClient.waitForSidechainTx(_, _, _) >> {
			// Once the join transaction has gone through, member status turns to active
			memberActive = true
		}
		du.isMemberActive(memberAddress) >> {
			memberActive
		}
		du.addMembers(memberAddress) >> Mock(EthereumTransactionReceipt)

		when:
		service.update(contractAddress, r.id, cmd)
		then:
		(1.._) * productService.findByBeneficiaryAddress(_) >> product
		(1.._) * streamrClientMock.dataUnionClient(_, _) >> duClient
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
