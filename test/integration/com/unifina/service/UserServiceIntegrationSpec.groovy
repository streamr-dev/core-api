package com.unifina.service

import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Category
import com.unifina.domain.marketplace.FreeSubscription
import com.unifina.domain.marketplace.PaidSubscription
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecRole
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SecUserSecRole
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.ModuleCategory
import com.unifina.domain.tour.TourUser
import grails.test.spock.IntegrationSpec

class UserServiceIntegrationSpec extends IntegrationSpec {
	UserService service
	SecUser user
	TourUser tour
	IntegrationKey ik
	Key key
	Canvas c
	Permission p
	SecRole role
	SecUserSecRole userRole
	ModuleCategory modCat
	Module module
	Feed feed
	Stream stream
	Category category
	Product free
	Product paid
	PaidSubscription ps
	FreeSubscription fs

	void setup() {
		service = new UserService()
		service.subscriptionService = new SubscriptionService()
		service.productService = new ProductService()
		service.productService.subscriptionService = new SubscriptionService()

		user = new SecUser()
		user.name = "Mickey Mouse"
		user.username = "mickey@mouse.com"
		user.password = "xxx"
		user.enabled = false
		user.save(validate: true, failOnError: true)

		tour = new TourUser()
		tour.tourNumber = 1
		tour.user = user
		tour.completedAt = new Date()
		tour.save(validate: true, failOnError: true)

		ik = new IntegrationKey()
		ik.user = user
		ik.name = "private key"
		ik.service = IntegrationKey.Service.ETHEREUM_ID
		ik.idInService = "0x0000000000000000000000000000000000000000"
		ik.json = "{}"
		ik.save(validate: true, failOnError: true)

		key = new Key()
		key.name = "My key"
		key.user = user
		key.save(validate: true, failOnError: true)

		c = new Canvas()
		c.name = "Untitled canvas"
		c.save(validate: true, failOnError: true)

		p = new Permission()
		p.user = user
		p.operation = Permission.Operation.READ
		p.canvas = c
		p.save(validate: true, failOnError: true)

		role = new SecRole()
		role.authority = "group"
		role.save(validate: true, failOnError: true)

		userRole = new SecUserSecRole()
		userRole.secUser = user
		userRole.secRole = role
		userRole.save(validate: true, failOnError: true)

		modCat = new ModuleCategory()
		modCat.name = "Module category"
		modCat.save(validate: true, failOnError: true)

		module = new Module()
		module.name = "Module"
		module.implementingClass = "java.lang.Object"
		module.jsModule = "javascript"
		module.type = "type"
		module.category = modCat
		module.save(validate: true, failOnError: true)

		feed = new Feed()
		feed.name =  "data feed"
		feed.module = module
		feed.eventRecipientClass = "java.lang.Object"
		feed.keyProviderClass = "java.lang.Object"
		feed.messageSourceClass = "java.lang.Object"
		feed.parserClass = "java.lang.Object"
		feed.streamListenerClass = "java.lang.Object"
		feed.timezone = "Europe/Helsinki"
		feed.save(validate: true, failOnError: true)

		stream = new Stream()
		stream.id = "stream-1"
		stream.feed = feed
		stream.save(validate: true, failOnError: true)

		category = new Category()
		category.id = "cat-1"
		category.name = "Product category"
		category.save(validate: true, failOnError: true)

		free = new Product()
		free.category = category
		free.owner = user
		free.pricePerSecond = 0
		free.addToStreams(stream)
		free.save(validate: true, failOnError: true)

		fs = new FreeSubscription()
		fs.user = user
		fs.product = free
		fs.endsAt = new Date()
		fs.save(validate: true, failOnError: true)

		paid = new Product()
		paid.category = category
		paid.owner = user
		paid.pricePerSecond = 1
		paid.ownerAddress = "0x0000000000000000000000000000000000000000"
		paid.beneficiaryAddress = "0x0000000000000000000000000000000000000000"
		paid.addToStreams(stream)
		paid.save(validate: true, failOnError: true)

		ps = new PaidSubscription()
		ps.address = "0x0000000000000000000000000000000000000000"
		ps.product = paid
		ps.endsAt = new Date()
		ps.save(validate: true, failOnError: true)
	}

	void "test user account removal"() {
		when:
		service.delete(user)

		then:
		Permission.findById(p.id) == null
		Key.findById(key.id) == null
		FreeSubscription.findById(fs.id) == null
		PaidSubscription.findById(ps.id) == null
		IntegrationKey.findById(ik.id) == null
		Product.findById(paid.id) == null
		Product.findById(free.id) == null
		SecUser.findById(user.id) == null
		TourUser.findById(tour.id) == null
		SecUserSecRole.findById(userRole.id) == null

		Canvas.findById(c.id) != null
		SecRole.findById(role.id) != null
		Category.findById(category.id) != null
		ModuleCategory.findById(modCat.id) != null
		Module.findById(module.id) != null
		Feed.findById(feed.id) != null
		Stream.findById(stream.id) != null
	}
}
