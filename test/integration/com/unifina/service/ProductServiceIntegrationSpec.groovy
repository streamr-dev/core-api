package com.unifina.service

import com.unifina.domain.*
import grails.test.spock.IntegrationSpec

class ProductServiceIntegrationSpec extends IntegrationSpec {
	static transactional = true
	ProductService service
	Category category
	Stream s1, s2, s3, s4
	Product product
	User troll
	User user
	Product p1
	Product p2
	Subscription fs1
	Subscription fs2

	void setup() {
		category = new Category(name: "Category")
		category.id = "category-id"
		category.save()

		s1 = new Stream(name: "stream-1")
		s2 = new Stream(name: "stream-2")
		s3 = new Stream(name: "stream-3")
		s4 = new Stream(name: "stream-4")
		[s1, s2, s3, s4].eachWithIndex { Stream s, int i -> s.id = "stream-id-${i + 1}" }
		[s1, s2, s3, s4]*.save(failOnError: true, validate: true)

		troll = new User(username: "0x0000000000000000000000000000000000000005", name: "sylvester stallone", email: "s@s.com")
		troll.save(failOnError: true, validate: false)
		user = new User(username: "0x0000000000000000000000000000000000000006", name: "arnold schwarzenegger", email: "a@schwarzenegger.com")
		user.save(failOnError: true, validate: false)
		p1 = new Product(
			name: "troll product",
			description: "description",
			ownerAddress: null,
			beneficiaryAddress: null,
			streams: [s1, s2],
			pricePerSecond: 0,
			category: category,
			state: Product.State.NOT_DEPLOYED,
			blockNumber: 40000,
			blockIndex: 30,
			owner: troll,
		)
		p1.save(failOnError: true, validate: true)
		p2 = new Product(
			name: "troll product 2",
			description: "description",
			ownerAddress: "0x0000000000000000000000000000000000000000",
			beneficiaryAddress: "0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
			streams: [s3],
			pricePerSecond: 10,
			category: category,
			state: Product.State.NOT_DEPLOYED,
			blockNumber: 40000,
			blockIndex: 30,
			owner: troll,
		)
		p2.save(failOnError: true, validate: true)

		fs1 = new SubscriptionFree(product: p1, user: troll, endsAt: new Date())
		fs1.save(failOnError: true, validate: true)
		fs2 = new SubscriptionFree(product: p1, user: user, endsAt: new Date())
		fs2.save(failOnError: true, validate: true)
	}

	void cleanup() {
	}

	void "remove users products and related entities"() {
		service = new ProductService()
		service.subscriptionService = new SubscriptionService()
		service.subscriptionService.permissionService = new PermissionService()

		when:
		service.removeUsersProducts("0x0000000000000000000000000000000000000005")

		then:
		Product.get(p1.id) == null
		Product.get(p2.id) == null
		SubscriptionFree.get(fs1.id) == null
		SubscriptionFree.get(fs2.id) == null

		Stream.get(s1.id) != null
		Stream.get(s2.id) != null
		Stream.get(s3.id) != null
		Stream.get(s4.id) != null
		Category.get(category.id) != null
	}
}
