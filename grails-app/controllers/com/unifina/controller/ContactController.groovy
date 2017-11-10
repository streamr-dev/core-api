package com.unifina.controller

import com.mashape.unirest.http.Unirest
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
class ContactController {

	def mailService

	@Secured("IS_AUTHENTICATED_ANONYMOUSLY")
	def send() {
//		String verificationUrl = grailsApplication.config.recaptcha.verifyUrl
//		String secretParam = grailsApplication.config.recaptchainvisible.secret
//		String responseParam = params."g-recaptcha-response"

//		def response = Unirest.post(verificationUrl)
//			.field("secret", secretParam)
//			.field("response", responseParam)
//			.asJson()
//		if (response.body.jsonObject.success == true) {
			mailService.sendMail {
				from grailsApplication.config.unifina.email.sender
				to grailsApplication.config.unifina.email.sender
				subject "Landing page message"
				body "Name: $params.contactName\nEmail: $params.contactEmail\nMessage: $params.contactMessage"
			}
			render ""
//		} else {
//			render (success:false, status:403) as JSON
//		}
	}
}
