package com.unifina.controller.feedback

import grails.test.mixin.TestFor
import spock.lang.Specification

import com.unifina.controller.feedback.FeedbackController
import com.unifina.signalpath.messaging.MockMailService
@TestFor(FeedbackController)


class FeedbackControllerSpec extends Specification{

	MockMailService ms
	
	def springSecurityService = [
		encodePassword: { pw -> return pw+"-encoded" },
		currentUser: [username: 'feed@back.com']
	]


	void setup() {
		grailsApplication.config.unifina.email.sender = "sender"
		grailsApplication.config.unifina.email.feedback.recipient = "recipient"
		
		defineBeans {
			mailService(MockMailService)
		}
		ms = grailsApplication.mainContext.getBean("mailService")
		assert ms != null
		
		controller.springSecurityService = springSecurityService

	}

	void "feedback email should be sent"(){
		when: "feedback sent from the feedback page"
			params.feedback = "Very accurate and helping feedback"
			request.method = 'POST'
			controller.send()
		then: "email should be sent"
			ms.mailSent
		then: "sender is correct"
			ms.from == "sender"
		then: "recipient must be correct"
			ms.to == "recipient"
		then: "subject must be correct"
			ms.subject == "Feedback from $springSecurityService.currentUser.username"
		then: "body must contain the feedback"
			ms.body.contains(params.feedback)
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
