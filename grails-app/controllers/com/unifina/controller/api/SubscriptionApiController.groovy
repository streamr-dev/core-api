package com.unifina.controller.api

import com.unifina.api.CreateSubscriptionCommand
import com.unifina.api.NotPermittedException
import com.unifina.api.ValidationException
import com.unifina.domain.security.SecUser
import com.unifina.security.AuthLevel
import com.unifina.security.StreamrApi
import com.unifina.service.SubscriptionService
import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.annotation.Secured

@Secured(["IS_AUTHENTICATED_ANONYMOUSLY"])
class SubscriptionApiController {
	SubscriptionService subscriptionService

	@GrailsCompileStatic
	@StreamrApi(authenticationLevel = AuthLevel.USER)
	def save(CreateSubscriptionCommand command) {
		if (!command.validate()) {
			throw new ValidationException(command.errors)
		}
		verifyDevops(loggedInUser())
		subscriptionService.onSubscribed(command.product, command.address, new Date(command.endsAt * 1000))
		render(status: 204)
	}

	@GrailsCompileStatic
	private static void verifyDevops(SecUser currentUser) {
		if (!currentUser.isDevOps()) {
			throw new NotPermittedException("DevOps role required")
		}
	}

	SecUser loggedInUser() {
		request.apiUser
	}
}
