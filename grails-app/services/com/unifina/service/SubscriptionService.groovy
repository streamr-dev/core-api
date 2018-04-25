package com.unifina.service

import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.FreeSubscription
import com.unifina.domain.marketplace.PaidSubscription
import com.unifina.domain.marketplace.Product
import com.unifina.domain.marketplace.Subscription
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class SubscriptionService {
	PermissionService permissionService

	void deleteProduct(Product product) {
		Subscription.findAllByProduct(product).toArray().each { Subscription subscription ->
			deletePermissions(subscription)
			subscription.delete()
		}
	}

	List<Subscription> getSubscriptionsOfUser(SecUser user) {
		List<IntegrationKey> integrationKeys = IntegrationKey.findAllByUserAndService(user, IntegrationKey.Service.ETHEREUM_ID)
		def addresses = integrationKeys*.idInService
		List<Subscription> subscriptions = new ArrayList<>()
		subscriptions.addAll(PaidSubscription.findAllByAddressInList(addresses))
		subscriptions.addAll(FreeSubscription.findAllByUser(user))
		return subscriptions
	}

	/**
	 * Should be invoked after marketplace smart contract event `Subscribed` has been emitted.
	 */
	Subscription onSubscribed(Product product, String address, Date endsAt) {
		Subscription subscription = PaidSubscription.findByProductAndAddress(product, address)
		if (subscription == null) {
			subscription = new PaidSubscription(product: product, address: address)
		}
		return updateSubscriptionAndLinkedPermissions(subscription, endsAt)
	}

	/**
	 * Subscribe to user to (free) Product
	 */
	Subscription subscribeToFreeProduct(Product product, SecUser user, Date endsAt) {
		FreeProductService.verifyThatProductIsFree(product)
		Subscription subscription = FreeSubscription.findByProductAndUser(product, user)
		if (subscription == null) {
			subscription = new FreeSubscription(product: product, user: user)
		}
		return updateSubscriptionAndLinkedPermissions(subscription, endsAt)
	}

	/**
	 * Should be invoked before an `IntegrationKey` is removed
	 */
	void beforeIntegrationKeyRemoved(IntegrationKey key) {
		verifyIsEthereumID(key)
		List<Subscription> subscriptions = PaidSubscription.findAllByAddress(key.idInService)
		subscriptions.each {
			deletePermissions(it)
		}
	}

	/**
	 * Should be invoked after an `IntegrationKey` is added
	 */
	void afterIntegrationKeyCreated(IntegrationKey key) {
		verifyIsEthereumID(key)
		List<Subscription> subscriptions = PaidSubscription.findAllByAddress(key.idInService)
		subscriptions.each {
			createPermissions(it)
		}
	}

	/**
	 * Should be invoked after a Product has been updated
	 */
	void afterProductUpdated(Product product) {
		Set<Stream> after = product.streams

		List<Subscription> subscriptions = Subscription.findAllByProduct(product)

		subscriptions.each { Subscription subscription ->
			Set<Stream> before = streamPermissionsFor(subscription)*.stream as Set
			deletePermissions(subscription, before - after)
			createPermissions(subscription, after - before)
		}
	}

	private Subscription updateSubscriptionAndLinkedPermissions(Subscription subscription, Date endsAt) {
		subscription.endsAt = endsAt
		subscription.save(failOnError: true)
		deletePermissions(subscription) // TODO: could be optimized to only remove/add what is necessary
		createPermissions(subscription)
		return subscription
	}

	private static void deletePermissions(Subscription subscription) {
		streamPermissionsFor(subscription)*.delete()
	}

	private static void deletePermissions(Subscription subscription, Set<Stream> streams) {
		List<Permission> permissions = Permission.findAllBySubscriptionAndStreamInList(subscription, streams as List)
		permissions*.delete()
	}

	private void createPermissions(Subscription subscription) {
		createPermissions(subscription, subscription.product.streams)
	}

	private void createPermissions(Subscription subscription, Set<Stream> streams) {
		SecUser user = subscription.fetchUser()
		if (user) {
			streams.collect { Stream stream ->
				Permission permission = permissionService.systemGrant(user, stream, Permission.Operation.READ)
				permission.subscription = subscription
				permission.endsAt = subscription.endsAt
				permission.save(failOnError: true)
			}
		}
	}

	private static Set<Permission> streamPermissionsFor(Subscription subscription) {
		Permission.findAllBySubscriptionAndStreamIsNotNull(subscription) as Set
	}


	private static void verifyIsEthereumID(IntegrationKey key) {
		if (key.service != IntegrationKey.Service.ETHEREUM_ID) {
			throw new IllegalArgumentException("key.service != ETHEREUM_ID")
		}
	}
}
