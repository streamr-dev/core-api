package com.unifina.controller.security

import grails.plugin.springsecurity.annotation.Secured
import com.unifina.domain.security.SecUser
import grails.transaction.Transactional
import com.unifina.domain.security.BillingAccount

@Transactional
@Secured(["IS_AUTHENTICATED_FULLY"])
class BillingAccountController {

	def springSecurityService
	def userService
	def billingAccountService
	def billingAccount

	static defaultAction = "edit"

	//Todo fetch these from config
	static DIRECT_API_ID = 'f33e8d60-258b-0134-f468-0aabff73eae9'
	static DIRECT_API_SECRET = 'yUkmekMjuRVVLVjz11f5pkIUVA4GFSKaMU6cfg'
	static DIRECT_API_PWD	= 'da8Hl7URArXuSorGCQDrmcX662SSTK623OEZ5qoZIA'
	//Todo redirect back to current page
	static REDIRECT_URI = 'redirect_uri='
	static NONCE 	= 'SECRET'


    def edit() {
		//Todo catch url parameters, find if redirected?
		//Todo Check if user has a billing account, if not then ask user to make one
		//Todo if user has billing account, show upgrade/downgrade options
		def params = params
		def user = SecUser.get(springSecurityService.currentUser.id)
		def products = billingAccountService.getProducts()
		def statements
		def data = REDIRECT_URI
		//todo what if user has been added to billing account, we should check instead the if user has a BA
		def subscriptions = billingAccountService.getSubscriptionsByCustomerReference(user.username)

		if (!user.billingAccount){
			flash.message = "User "+ user.username + " has no Billing Account" + "</br>" + "Create a new Billing Account by selecting a plan and subscribing or join an existing Billing Account"
		} else if (subscriptions){
			statements = billingAccountService.getStatements(subscriptions.subscription.id)
			data = data + '&amp;subscription_id=' + subscriptions.subscription.id
		}

		if(params.containsKey("status_code") && params.containsKey("result_code") && params.containsKey("call_id")){
			def callContent = billingAccountService.getCall(params.call_id)
			if (params.status_code == "422"){
				if(callContent){
					def errors = callContent.call.response.result.errors
					def errorMsg = 'Following errors occured while trying to send a form: </br>'
					//todo check how to loop json object attrs
					errors.each{
						errorMsg = errorMsg  + it.message + "</br>"
					}
					flash.error = errorMsg
				}
			}
			else if (params.status_code == '401') {
				//def call = billingAccountService.getCall(params['call_id'])

				flash.error = "Something went wrong, please try again.."
			}

			else if (params.status_code == "200" && params.result_code == "2000"){
				//todo check from call id what was done..?
				//check if user has billing account attached to his domain model
				//if user has billing account, updated billing account information if necessary

				//check from call, was edit succesful

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
					def billingAccount = new BillingAccount(chargifySubscriptionId: subscriptions.subscription.id,chargifyCustomerId: subscriptions.subscription.customer.id)
					billingAccount.save()
					user.billingAccount = billingAccount
					user.save()
					flash.message = "Subscription successful!"
				}
			}
		}



		//def productFamilies = billingAccountService.getProductFamilies()

		//def productFamilyComponents = billingAccountService.getProductFamilyComponents('566553')


		def now = new Date()

		long timestamp = System.currentTimeMillis() / 1000L
		[user:SecUser.get(springSecurityService.currentUser.id), products:products,
		hmac:billingAccountService.hmac(DIRECT_API_ID+timestamp+NONCE+data, DIRECT_API_SECRET),
		apiId:DIRECT_API_ID, timestamp: timestamp, nonce: NONCE, data: data,
		subscriptions: subscriptions, statements: statements, errors: errors
		]
	}

	def update() {

		def user = SecUser.get(springSecurityService.currentUser.id)
		def params = params
		//todo get product id values
		//todo check how subscription post works
		if (user.billingAccount.chargifySubscriptionId && params['signup[product][handle]']){
			def productHandle = params['signup[product][handle]']
			def content = billingAccountService.updateSubscription(user.billingAccount.chargifySubscriptionId, productHandle)

			if (content){
				flash.message = 'Subscription updated to ' + content.subscription.product.name + ' plan'
			}
			else {
				flash.error = 'Something went wrong, please try to update again'
			}
		}

		redirect(action:"edit")
	}

	def updateCreditCardInformation(){
		def params = params
		def user = SecUser.get(springSecurityService.currentUser.id)

		if (user.billingAccount.chargifySubscriptionId) {
			//get payment profile id from billing account
			//update payment profile
			//

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
}
