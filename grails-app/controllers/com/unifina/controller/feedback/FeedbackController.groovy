package com.unifina.controller.feedback

import grails.plugin.springsecurity.annotation.Secured

@Secured(["ROLE_USER"])
class FeedbackController {

	def mailService
	def springSecurityService
	
	def index(){}
	
	def send(){
		mailService.sendMail {
			from grailsApplication.config.unifina.email.sender
			to grailsApplication.config.unifina.email.feedback.recipient
			subject "Feedback from $springSecurityService.currentUser.username"
			body "From:\n$springSecurityService.currentUser.username \n\nFeedback:\n$params.feedback"
		}
		flash.message = "Thank you for your feedback!"
		redirect(action:"index")
	}
}
