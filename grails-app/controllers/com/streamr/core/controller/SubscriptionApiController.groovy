package com.streamr.core.controller

import com.streamr.core.domain.User
import com.streamr.core.service.NotPermittedException
import com.streamr.core.service.SubscriptionService
import com.streamr.core.service.ValidationException
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON

class SubscriptionApiController {
	SubscriptionService subscriptionService

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def index() {
		def subscriptions = subscriptionService.getSubscriptionsOfUser(loggedInUser())
		render(subscriptions*.toMap() as JSON)
	}

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def save(CreateSubscriptionCommand command) {
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}

		if (command.address) {
			verifyDevops(loggedInUser())
			subscriptionService.onSubscribed(command.product, command.address, command.endsAtAsDate)
		} else {
			subscriptionService.subscribeToFreeProduct(command.product, loggedInUser(), command.endsAtAsDate)
		}

		render(status: 204)
	}

	@GrailsCompileStatic
	private static void verifyDevops(User currentUser) {
		if (!currentUser.isDevOps()) {
			throw new NotPermittedException("DevOps role required")
		}
	}

	User loggedInUser() {
		request.apiUser
	}
}
