package com.unifina.service

import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Category
import com.unifina.domain.marketplace.FreeSubscription
import com.unifina.domain.marketplace.Product
import com.unifina.domain.marketplace.Subscription
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.ModuleCategory
import grails.test.spock.IntegrationSpec

class ProductServiceIntegrationSpec extends IntegrationSpec {
	static transactional = true
	ProductService service
	Category category
	Stream s1, s2, s3, s4
	Product product
	ModuleCategory mc
	Module module
	Feed feed
	SecUser troll
	SecUser user
	Product p1
	Product p2
	Subscription fs1
	Subscription fs2

	void setup() {
		category = new Category(name: "Category")
		category.id = "category-id"
		category.save()
		mc = new ModuleCategory(name: "module category")
		mc.save(failOnError: true, validate: true)
		module = new Module(
			name: "module name",
			alternativeNames: "alt names",
			implementingClass: "x",
			jsModule: "jsmodule",
			category: mc,
			type: "type"
		)
		module.save(failOnError: true, validate: true)
		feed = new Feed(
			name: "feed name",
			eventRecipientClass: "x",
			streamListenerClass: "x",
			keyProviderClass: "x",
			messageSourceClass: "x",
			parserClass: "x",
			timezone: "Europe/Helsinki",
			module: module
		)
		feed.save(failOnError: true, validate: false)
		s1 = new Stream(name: "stream-1", feed: feed)
		s2 = new Stream(name: "stream-2", feed: feed)
		s3 = new Stream(name: "stream-3", feed: feed)
		s4 = new Stream(name: "stream-4", feed: feed)
		[s1, s2, s3, s4].eachWithIndex { Stream s, int i -> s.id = "stream-id-${i+1}" }
		[s1, s2, s3, s4]*.save(failOnError: true, validate: true)

		troll = new SecUser(username: "sylvester", name: "sylvester stallone", password: "x", email: "s@s.com", timezone: "Europe/Helsinki")
		troll.save(failOnError: true, validate: false)
		user = new SecUser(username: "arnold", name: "arnold schwarzenegger", password: "x", email: "a@schwarzenegger.com", timezone: "Europe/Helsinki")
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
			owner: troll
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
			owner: troll
		)
		p2.save(failOnError: true, validate: true)

		new IntegrationKey(
			user: troll,
			name: "ik1",
			service: IntegrationKey.Service.ETHEREUM_ID,
			idInService: "0x0000000000000000000000000000000000000005",
			json: "{}"
		).save(failOnError: true, validate: false)

		fs1 = new FreeSubscription(product: p1, user: troll, address: "0x0000000000000000000000000000000000000005", endsAt: new Date())
		fs1.save(failOnError: true, validate: true)
		fs2 = new FreeSubscription(product: p1, user: user, address: "0x0000000000000000000000000000000000000005", endsAt: new Date())
		fs2.save(failOnError: true, validate: true)
	}

	void cleanup() {
	}

	void "remove users products and related entities"() {
		service = new ProductService()
		service.subscriptionService = new SubscriptionService()
		service.subscriptionService.permissionService = new PermissionService()

		when:
		service.removeUsersProducts("sylvester")

		then:
		Product.get(p1.id) == null
		Product.get(p2.id) == null
		FreeSubscription.get(fs1.id) == null
		FreeSubscription.get(fs2.id) == null

		Stream.get(s1.id) != null
		Stream.get(s2.id) != null
		Stream.get(s3.id) != null
		Stream.get(s4.id) != null
		Category.get(category.id) != null
		ModuleCategory.get(mc.id) != null
		Module.get(module.id) != null
		Feed.get(feed.id) != null
	}
}
