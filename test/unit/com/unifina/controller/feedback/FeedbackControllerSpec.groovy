package com.unifina.controller.feedback

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.*
import spock.lang.Specification

import com.unifina.domain.security.SecUser
import com.unifina.signalpath.messaging.MockMailService

@TestFor(FeedbackController)
class FeedbackControllerSpec extends Specification{
	
	def springSecurityService = [
		encodePassword: {pw -> return pw+"-encoded" },
		getCurrentUser: {-> new SecUser(username: 'feed@back.com')}
	] as SpringSecurityService

	void setup() {
		grailsApplication.config.unifina.email.sender = "sender"
		grailsApplication.config.unifina.email.feedback.recipient = "recipient"
				
		controller.springSecurityService = springSecurityService
		controller.mailService = new MockMailService()
	}

	void "feedback email should be sent"() {
		when: "feedback sent from the feedback page"
			params.feedback = "Very accurate and helping feedback"
			request.method = 'POST'
			controller.send()
		then: "email should be sent"
			controller.mailService.mailSent
		then: "sender is correct"
			controller.mailService.from == "sender"
		then: "recipient must be correct"
			controller.mailService.to == "recipient"
		then: "subject must be correct"
			controller.mailService.subject == "Feedback from $springSecurityService.currentUser.username"
		then: "body must contain the feedback"
			controller.mailService.body.contains(params.feedback)
	}
	
	class MailDelegate {
		def from(s) {
			from = s
		}
		def to(s) {
			to = s
		}
		def subject(s) {
			subject = s
		}
		def body(s) {
			body = s
		}
	}
}
