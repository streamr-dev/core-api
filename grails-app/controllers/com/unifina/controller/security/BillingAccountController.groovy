package com.unifina.controller.security

import grails.plugin.springsecurity.annotation.Secured
import com.unifina.domain.security.SecUser
import grails.transaction.Transactional
import com.unifina.domain.security.BillingAccount
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import com.unifina.domain.security.BillingAccountInvite

import org.apache.commons.validator.EmailValidator

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

    def edit() {

		def user = SecUser.get(springSecurityService.currentUser.id)
		def products = billingAccountService.getProducts()
		def statements
		def data = REDIRECT_URI
		def subscriptions = billingAccountService.getSubscriptionsByCustomerReference(user.username)
		def billingAccountUsers = []

		if (!user.billingAccount){
			flash.message = "User "+ user.username + " has no Billing Account" + "</br>" + "Create a new Billing Account by selecting a plan and subscribing or join an existing Billing Account"
		} else if (subscriptions.code == 200){
			statements = billingAccountService.getStatements(subscriptions.content.subscription.id)
			data = data + '&amp;subscription_id=' + subscriptions.content.subscription.id
		}

		if (user.billingAccount) {
			billingAccountUsers = SecUser.findAllByBillingAccount(user.billingAccount)
		}

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

				if(user.billingAccount){
					//todo we should show different form/view in billingAccount.gsp for migrations
					//endpoint is different for migrations than it is for creating subscriptions
					//https://docs.chargify.com/api-migrations

					//migration needs information about product handle or product id,
					//or should we do upgrade/downgrade on backend-side and show only options on view

				}
				else if (subscriptions) {
					//todo check if subscription exists in billing account table
					//if not create a new billing account with subscription
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

	def emailInvite() {
		def user = SecUser.get(springSecurityService.currentUser.id)

		//todo validate that email has at least a correct form
		if (params['emailInvite']){
			EmailValidator emailValidator = EmailValidator.getInstance()
			if (!emailValidator.isValid(params['emailInvite'])){
				flash.error = 'Invalid email address'
			}
			else {
				def billingAccountInvite = new BillingAccountInvite(billingAccount: user.billingAccount, users: user)
				billingAccountInvite.save()

				mailService.sendMail {
					from grailsApplication.config.unifina.email.sender
					to params['emailInvite']
					subject 'You have been invited to Streamr Billing Account'
					html g.render(template: "email_invite", model: [user: user, token: billingAccountInvite.token], plugin: 'unifina-core')
				}
				flash.message = 'Invite sent to ' + params['emailInvite']
			}
		} else {
			flash.error = 'Something went wrong, please try again soon'
		}
		redirect(action:"edit")
	}

	def joinToBillingAccount() {
		//todo check if user has an account, if not then reroute user to create an account / or check at email send phase if user has an account and give option to create one, then follow billingAccount link
		//todo check if user is already in a billing account, what do we do then ?
		//def params = params
		def user = SecUser.get(springSecurityService.currentUser.id)

		//todo refactor parameter to some other variable/namespace
		if (params['token']){
			def billingAccountInvite = BillingAccountInvite.findByToken(params['token'])

			if (!billingAccountInvite || billingAccountInvite.used){
				flash.error = 'Billing Account invite has already been used'
			}
			else if (billingAccountInvite && !billingAccountInvite.used) {
				billingAccountInvite.used = Boolean.TRUE
				billingAccountInvite.save()

				def baId = billingAccountInvite.billingAccount.id
				def ba = BillingAccount.findById(baId)

				user.billingAccount = ba
				user.save()
				flash.message = 'You have joined succesfully to Billing Account'
			}
			else {
				flash.error = 'Your account is already in a billing account'
			}
		}
		redirect(action:"edit")
	}

	def update() {

		def user = SecUser.get(springSecurityService.currentUser.id)
		def params = params
		//todo get product id values
		//todo check how subscription post works
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
