package com.streamr.core.service

import com.streamr.core.domain.*
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

	List<Subscription> getSubscriptionsOfUser(User user) {
		def addresses = [user?.getUsername()]
		List<Subscription> subscriptions = new ArrayList<>()
		subscriptions.addAll(SubscriptionPaid.findAllByAddressInList(addresses))
		subscriptions.addAll(SubscriptionFree.findAllByUser(user))
		return subscriptions
	}

	/**
	 * Should be invoked after marketplace smart contract event `Subscribed` has been emitted.
	 */
	Subscription onSubscribed(Product product, String address, Date endsAt) {
		Subscription subscription = SubscriptionPaid.findByProductAndAddress(product, address)
		if (subscription == null) {
			subscription = new SubscriptionPaid(product: product, address: address)
		}
		return updateSubscriptionAndLinkedPermissions(subscription, endsAt)
	}

	/**
	 * Subscribe to user to (free) Product
	 */
	Subscription subscribeToFreeProduct(Product product, User user, Date endsAt) {
		ProductService.verifyThatProductIsFree(product)
		Subscription subscription = SubscriptionFree.findByProductAndUser(product, user)
		if (subscription == null) {
			subscription = new SubscriptionFree(product: product, user: user)
		}
		return updateSubscriptionAndLinkedPermissions(subscription, endsAt)
	}

	/**
	 * Should be invoked after a Product has been updated
	 */
	void afterProductUpdated(Product product) {
	}

	private Subscription updateSubscriptionAndLinkedPermissions(Subscription subscription, Date endsAt) {
		subscription.endsAt = endsAt
		subscription.save(failOnError: true)
		deletePermissions(subscription) // TODO: could be optimized to only remove/add what is necessary
		//createPermissions(subscription)
		return subscription
	}

	private void deletePermissions(Subscription subscription) {
		for (Permission p : streamPermissionsFor(subscription)) {
			permissionService.systemRevoke(p)
		}
	}

	private static void deletePermissions(Subscription subscription, Set<String> streams) {
		// TODO
		//List<Permission> permissions = Permission.findAllBySubscriptionAndStreamInList(subscription, streams as List)
		//permissions*.delete()
		List<Permission> permissions = Permission.findAllBySubscription(subscription)
		permissions*.delete()

	}

	private void createPermissions(Subscription subscription) {
		//createPermissions(subscription, subscription.product.streams)
	}

	private void createPermissions(Subscription subscription, Set<String> streams) {
		User user = subscription.fetchUser()
		if (user) {
			streams.collect { String stream ->
				//permissionService.systemGrant(user, stream, Permission.Operation.STREAM_SUBSCRIBE, subscription, subscription.endsAt)
				//permissionService.systemGrant(user, stream, Permission.Operation.STREAM_GET, subscription, subscription.endsAt)
			}
		}
	}

	private static Set<Permission> streamPermissionsFor(Subscription subscription) {
		// TODO
		//return Permission.findAllBySubscriptionAndStreamIsNotNull(subscription) as Set
		return Permission.findAllBySubscription(subscription) as Set
	}
}
