package com.unifina.security

import grails.test.mixin.TestFor
import spock.lang.Specification

import com.unifina.controller.feedback.FeedbackController
@TestFor(FeedbackController)


class FeedbackControllerSpec extends Specification{

	boolean mailSent
	String from
	String to
	String subject
	String body
	
	def springSecurityService = [
		encodePassword: { pw -> return pw+"-encoded" },
		currentUser: [username: 'feed@back.com']
	]


	void setup() {
		grailsApplication.config.unifina.email.sender = "sender"
		grailsApplication.config.unifina.email.feedback.recipient = "recipient"
		
		mailSent = false
		from = null
		to = null
		subject = null
		body = null
		
		controller.springSecurityService = springSecurityService
		
		controller.mailService = [
			sendMail: {Closure c-> 
				mailSent = true 
				def delegate = new MailDelegate()
				c.delegate = delegate
				c.call(delegate)
			},
			grailsApplication: grailsApplication
		]
	}

	void "feedback email should be sent"(){
		when: "feedback sent from the feedback page"
			params.feedback = "Very accurate and helping feedback"
			request.method = 'POST'
			controller.send()
		then: "email should be sent"
			mailSent
		then: "sender is correct"
			from == "sender"
		then: "recipient must be correct"
			to == "recipient"
		then: "subject must be correct"
			subject == "Feedback from $springSecurityService.currentUser.username"
		then: "body must contain the feedback"
			body.contains(params.feedback)
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
