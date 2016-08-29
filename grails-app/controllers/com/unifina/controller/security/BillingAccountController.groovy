package com.unifina.controller.security

import grails.plugin.springsecurity.annotation.Secured
import com.unifina.domain.security.SecUser
import grails.transaction.Transactional
import com.unifina.domain.security.BillingAccount
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import com.unifina.domain.security.BillingAccountInvite

import org.apache.commons.validator.EmailValidator

import com.unifina.user.UserCreationFailedException

@Transactional

@Secured(["IS_AUTHENTICATED_FULLY"])
class BillingAccountController {

	def springSecurityService
	def userService
	def billingAccountService
	def billingAccount

	def mailService

	static defaultAction = "edit"

	static DIRECT_API_ID 		= "${CH.config.chargify.directApiId}"
	static DIRECT_API_SECRET 	= "${CH.config.chargify.directApiSecret}"
	static DIRECT_API_PWD		= "${CH.config.chargify.directApiPwd}"
	static REDIRECT_URI 		= 'redirect_uri='
	static NONCE 				= 'SECRET'

	def create() {
		def user = SecUser.get(springSecurityService.currentUser.id)
		def products = billingAccountService.getProducts()
		def data = REDIRECT_URI
		def subscriptions = billingAccountService.getSubscriptionsByCustomerReference(user.username)

		if (user.billingAccount) {
			redirect action: "edit"
		} else {
			//check if we have been redirected to page and we have a call id to request which was made to chargify v2 api
			if(params.containsKey("status_code") && params.containsKey("result_code") && params.containsKey("call_id")) {
				def call = billingAccountService.getCall(params.call_id)
				if (params.status_code == "422"){
					if(call.content){
						def errors = call.content.call.response.result.errors
						def errorMsg = 'Following errors occured while trying to send a form: </br>'
						errors.each{
							errorMsg = errorMsg  + it.message + "</br>"
						}
						flash.error = errorMsg
					}
				}
				else if (params.status_code == '401') {
					flash.error = "Something went wrong, please try again.."
				}
				else if (params.status_code == "200" && params.result_code == "2000"){
					flash.message = "Update successful!"

					//if user doesn't have a billing account but we have a subscription for him, create a billing account
					if (!user.billingAccount && subscriptions) {
						def billingAccount = new BillingAccount(chargifySubscriptionId: subscriptions.content.subscription.id, chargifyCustomerId: subscriptions.content.subscription.customer.id)
						billingAccount.save()
						user.billingAccount = billingAccount
						user.save()
						flash.message = "Subscription successful!"
						redirect action: "edit"
					}
				}
			} else {
				flash.message = "User "+ user.username + " has no Billing Account" + "</br>" + "Create a new Billing Account by selecting a plan and subscribing or ask for an invite to join a Billing Account"
			}
			long timestamp = System.currentTimeMillis() / 1000L
			[user:SecUser.get(springSecurityService.currentUser.id), products: products.content,
			 hmac:billingAccountService.hmac(DIRECT_API_ID+timestamp+NONCE+data, DIRECT_API_SECRET),
			 apiId:DIRECT_API_ID, timestamp: timestamp, products: products.content, nonce: NONCE, data: data, errors: errors]
		}

	}
	
    def edit() {
		def user = SecUser.get(springSecurityService.currentUser.id)
		def products = billingAccountService.getProducts()
		def statements
		def data = REDIRECT_URI
		def subscriptions = billingAccountService.getSubscriptionsByCustomerReference(user.username)
		def billingAccountUsers = []

		//check if user has a billing account
		if (!user.billingAccount){
			redirect action: "create"
		} else {
			billingAccountUsers = SecUser.findAllByBillingAccount(user.billingAccount)

			if (subscriptions.code == 200){
				statements = billingAccountService.getStatements(subscriptions.content.subscription.id)
				data = data + '&amp;subscription_id=' + subscriptions.content.subscription.id
			}
		}

		//check if we have been redirected to page and we have a call id to request which was made to chargify v2 api
		if(params.containsKey("status_code") && params.containsKey("result_code") && params.containsKey("call_id")){
			def call = billingAccountService.getCall(params.call_id)
			if (params.status_code == "422"){
				if(call.content){
					def errors = call.content.call.response.result.errors
					def errorMsg = 'Following errors occured while trying to send a form: </br>'
					errors.each{
						errorMsg = errorMsg  + it.message + "</br>"
					}
					flash.error = errorMsg
				}
			}
			else if (params.status_code == '401') {
				flash.error = "Something went wrong, please try again.."
			}
			else if (params.status_code == "200" && params.result_code == "2000"){
				flash.message = "Update successful!"

				//if user doesn't have a billing account but we have a subscription for him, create a billing account
				if (!user.billingAccount && subscriptions) {
					def billingAccount = new BillingAccount(chargifySubscriptionId: subscriptions.content.subscription.id, chargifyCustomerId: subscriptions.content.subscription.customer.id)
					billingAccount.save()
					user.billingAccount = billingAccount
					user.save()
					flash.message = "Subscription successful!"
				}
			}
		}

		def now = new Date()

		long timestamp = System.currentTimeMillis() / 1000L
		[user:SecUser.get(springSecurityService.currentUser.id), products: products.content,
		hmac:billingAccountService.hmac(DIRECT_API_ID+timestamp+NONCE+data, DIRECT_API_SECRET),
		apiId:DIRECT_API_ID, timestamp: timestamp, nonce: NONCE, data: data,
		subscriptions: subscriptions.content, statements: statements, errors: errors, billingAccountUsers: billingAccountUsers
		]
	}
	
	def changeCreditCard() {
		def user = SecUser.get(springSecurityService.currentUser.id)
		def subscriptions = billingAccountService.getSubscriptionsByCustomerReference(user.username)
		def timestamp = new Date()
		def data = REDIRECT_URI + createLink(action:"edit", absolute: true).encodeAsURL() + '&amp;subscription_id=' + subscriptions.content.subscription.id
		[hmac:billingAccountService.hmac(DIRECT_API_ID+timestamp+NONCE+data, DIRECT_API_SECRET),
		 apiId:DIRECT_API_ID, timestamp: timestamp, nonce: NONCE, data: data,
		 subscriptions: subscriptions.content]
	}

	def emailInvite() {
		def user = SecUser.get(springSecurityService.currentUser.id)

		if (params['emailInvite']){
			EmailValidator emailValidator = EmailValidator.getInstance()

			if (!emailValidator.isValid(params['emailInvite'])){
				flash.error = 'Invalid email address'
			}
			//todo refactor so that only one type of email is sent, if there's an user with that username don't ask to register but add him to billing account and reroute to login auth
			else {
				def billingAccountInvite = new BillingAccountInvite(billingAccount: user.billingAccount, users: user, email: params['emailInvite'])
				billingAccountInvite.save()

				mailService.sendMail {
					from grailsApplication.config.unifina.email.sender
					to params['emailInvite']
					subject 'You have been invited to Streamr Billing Account'
					html g.render(template: "email_invite_register", model: [user: user, invite: billingAccountInvite.token], plugin: 'unifina-core')
				}
				flash.message = 'Billing Account invite sent to ' + params['emailInvite']
			}
		} else {
			flash.error = 'Something went wrong, please try again soon'
		}
		redirect(action:"edit")
	}

	@Secured(["permitAll"])
	def register(RegisterCommand cmd){
		def params = params
		def billingAccountInvite

		if (params.containsKey("invite") & params['invite'] != ""){
			billingAccountInvite = BillingAccountInvite.findByToken(params['invite'])

			def existingUser = SecUser.findByUsername(billingAccountInvite.email)

			if (existingUser && !billingAccountInvite.used && !existingUser.billingAccount) {
				billingAccountInvite.used = true
				existingUser.billingAccount = billingAccountInvite.billingAccount
				flash.message = 'You have been added to Billing Account, please login'
				return redirect(controller: 'login', action: 'auth', params: flash)
			}
		}

		if (!billingAccountInvite || billingAccountInvite.used){
			flash.message = 'Invalid Billing Account invite or invite has already been used'
			def token = !billingAccountInvite ? '' : billingAccountInvite.token
			def email = !billingAccountInvite ? '' : billingAccountInvite.email
			return render(view: '/register/register', model:[user: [username: email], invite: token])
		}

		if (request.method == "GET") {
			// activated the registration, but not yet submitted it
			return render(view: '/register/register', model: [user: [username: billingAccountInvite.email], invite: billingAccountInvite.token])
		}

		else if (request.method == "POST") {

			def user

			if (SecUser.findByUsername(billingAccountInvite.email)){
				flash.message = 'Username already exists'
				return render(view: '/register/register', model: [user: [username: billingAccountInvite.email], invite: billingAccountInvite.token])
			}

			//todo verify that this works
			if (cmd.hasErrors()) {
				flash.message = 'Something went wrong, please try again'
				log.warn("Registration command has errors: "+userService.checkErrors(cmd.errors.getAllErrors()))
				return render(view: '/register/register', model: [user: [username: billingAccountInvite.email], invite: billingAccountInvite.token])
				//return render(view: 'register', model: [user: cmd, invite: invite.code])
			}

			try {
				user = userService.createUser(cmd.properties)
				user.billingAccount = billingAccountInvite.getBillingAccount()
				billingAccountInvite.used = true
				//todo add user to billing account
				//todo mark billing account invite to used
			} catch (UserCreationFailedException e) {
				flash.message = e.getMessage()
				return render(view: '/register/register', model: [user: [username: billingAccountInvite.email], invite: billingAccountInvite.token])
			}
		}
		redirect(action:"edit")
	}

	def update() {
		def user = SecUser.get(springSecurityService.currentUser.id)
		if (user.billingAccount.chargifySubscriptionId && params['signup[product][handle]']){
			def productHandle = params['signup[product][handle]']
			def response = billingAccountService.updateSubscription(user.billingAccount.chargifySubscriptionId, productHandle)

			if (response.content){
				flash.message = 'Subscription updated to ' + response.content.subscription.product.name + ' plan'
			}
			else {
				flash.error = 'Something went wrong, please try to update again'
			}
		}
		redirect(action:"edit")
	}

	def addBillingAccountKey() {
		def user = SecUser.get(springSecurityService.currentUser.id)
		def params = params

		if (params['billingAccountKey']){
			def apiKey = params['billingAccountKey']
			def billingAccount = BillingAccount.findByApiKey(apiKey)

			if (billingAccount) {
				user.billingAccount = billingAccount
				user.save()
				flash.message = 'Billing Account added to user'
			}
			else {
				flash.error = 'Could not find Billing Account with key'
			}
		}
		redirect(action:"edit")
	}

	def statement() {
		def user = SecUser.get(springSecurityService.currentUser.id)
		if (params['statementId']){
			def subscriptions = billingAccountService.getSubscriptionsByCustomerReference(user.username)
			def statements = billingAccountService.getStatements(subscriptions.content.subscription.id)
			def content   = billingAccountService.getStatementPdf(params['statementId'])

			response.setHeader("Content-disposition", content.disposition)
			response.setHeader("Content-Length", "file-size")
			response.setContentType(content.contentType)
			response.outputStream << content.inputStream
			response.outputStream.flush()
			response.outputStream.close()
			return null
		}
	}
}