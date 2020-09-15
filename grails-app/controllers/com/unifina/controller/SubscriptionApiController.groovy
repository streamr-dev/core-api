package com.unifina.controller

import com.unifina.api.NotPermittedException
import com.unifina.api.ValidationException
import com.unifina.domain.User
import com.unifina.service.SubscriptionService
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
