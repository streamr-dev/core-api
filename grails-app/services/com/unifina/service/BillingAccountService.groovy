package com.unifina.service

import com.mashape.unirest.request.HttpRequest
import grails.transaction.Transactional
import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.*
import grails.converters.JSON

import grails.plugins.rest.client.RestBuilder
import grails.web.JSONBuilder

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.SSLContext
import java.security.SignatureException

import groovy.json.*

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

class BillingAccountService {

	public static final int HTTP_RESPONSE_CODE_OK = 200;
	public static final int HTTP_RESPONSE_CODE_UNPROCESSABLE_ENTITY = 422;

	public static final String POST   = "POST"
	public static final String GET    = "GET"
	public static final String PUT    = "PUT"
	public static final String DELETE = "DELETE"


	def grailsApplication
	//TODO move static variables to configs
	//TODO create wrapper for querying chargify

	//api v1
	public String API_KEY 							= CH.config.chargify.apiKey + CH.config.chargify.apiKeySuffix
	public String PRODUCTS_URL 		  				= "https://${CH.config.chargify.subDomain}.chargify.com/products"
	public String CUSTOMER_LOOKUP_URL 				= "https://${CH.config.chargify.subDomain}.chargify.com/customers/lookup.json?reference="
	public String SUBSCRIPTION_BY_CUSTOMER_ID_URL 	= "https://${CH.config.chargify.subDomain}.chargify.com/customers/"
	public String SUBSCRIPTIONS_URL 				= "https://${CH.config.chargify.subDomain}.chargify.com/subscriptions/"
	public String PRODUCT_FAMILY_URL 				= "https://${CH.config.chargify.subDomain}.chargify.com/product_families"

	//api v2
	public String DIRECT_API_ID     = "${CH.config.chargify.directApiId}"
	public String DIRECT_API_SECRET = "${CH.config.chargify.directApiSecret}"
	public String DIRECT_API_PWD	= "${CH.config.chargify.directApiPwd}"
	public String CHARGIFY_ACCOUNT  = "${CH.config.chargify.subDomain}"
	public String CALL_URL			= 'https://api.chargify.com/api/v2/calls/'



	def HttpURLConnection getChargifyConnection(String urlStr, String method) {
		URL url = new URL(urlStr)
		String encoded = new sun.misc.BASE64Encoder().encode((API_KEY)?.getBytes());
		HttpURLConnection conn = url.openConnection()
		conn.setRequestProperty('Accept','application/json')
		conn.setRequestProperty("Authorization", "Basic ${encoded}")
		conn.setRequestMethod(method)
		conn.doOutput = true
		return conn
	}

	def HttpURLConnection getChargifyV2Connection(String urlStr, String method) {
		URL url = new URL(urlStr)
		def auth = DIRECT_API_ID+':'+DIRECT_API_PWD
		String encoded = new sun.misc.BASE64Encoder().encode((DIRECT_API_ID+':'+DIRECT_API_PWD)?.getBytes());
		//java bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6459815
		encoded = encoded.replaceAll("\n", "");
		HttpURLConnection conn = url.openConnection()
		conn.setRequestProperty('Accept','application/json')
		conn.setRequestProperty("Authorization", "Basic ${encoded}")
		conn.setRequestMethod(method)
		conn.doOutput = true
		return conn
	}

	//https://docs.chargify.com/api-products
    def getProducts() {

		HttpURLConnection conn = getChargifyConnection(PRODUCTS_URL,GET)
		conn.connect()
		int responseCode = conn.getResponseCode()
		def content = []
		if(responseCode == HTTP_RESPONSE_CODE_OK) {
			content = JSON.parse(conn.content?.text)
		}
		//Todo add error handling
		return content
    }

	//https://docs.chargify.com/api-customers
	def getCustomerByReference(String reference) {

		HttpURLConnection conn = getChargifyConnection(CUSTOMER_LOOKUP_URL+reference,GET)
		conn.connect()
		int responseCode = conn.getResponseCode()
		//check response code
		def content = []
		if (responseCode == HTTP_RESPONSE_CODE_OK){
			content = JSON.parse(conn.content?.text)
		}
		//Todo add error handling
		return content
	}

	//https://docs.chargify.com/api-subscriptions
	def getSubscriptionsByCustomerReference(String reference) {
		def customer = getCustomerByReference(reference)
		def customerId
		if (customer){
			customerId = customer.customer.id
		}
		def subscriptions = getSubscriptionsByCustomerId(customerId)

		if (subscriptions){
			subscriptions = subscriptions.first()
		}

		//if customer object is not empty
		//get customer id

		return subscriptions
	}

	def getSubscriptionsByCustomerId(Id){
		def url = SUBSCRIPTION_BY_CUSTOMER_ID_URL+Id+"/subscriptions.json"
		HttpURLConnection conn = getChargifyConnection(url,GET)
		conn.connect()
		int responseCode = conn.getResponseCode()
		//check response code
		def content = []
		if (responseCode == HTTP_RESPONSE_CODE_OK){
			content = JSON.parse(conn.content?.text)
		}
		//Todo add error handling
		return content
	}

	//experimental
	def migrateSubscription(subscriptionId){
		def url = SUBSCRIPTIONS_URL+subscriptionId+"/migrations.json"
		HttpURLConnection conn = getChargifyConnection(url,POST)
		/*conn.setDoInput(true)*/
		//todo test api endpoint with curl/postman
		conn.setDoOutput(true)
		conn.setRequestMethod(POST)
		def json = new JsonBuilder()

			json.migration {
				product_id "3857125"
			}

		OutputStream os = conn.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
		osw.write(json.toString());
		osw.flush();
		osw.close();

		//conn.connect()
		int responseCode = conn.getResponseCode()
		//check response code
		def responseMsg = conn.responseMessage
		def errorStream = conn.getErrorStream()

		def content = []
		if (responseCode == HTTP_RESPONSE_CODE_OK){
			content = JSON.parse(conn.content?.text)
		}
		//Todo add error handling
		return content

	}

	//https://docs.chargify.com/api-metered-usage
	def getMeteredUsage(subscriptionId, componentId){
		def url = SUBSCRIPTIONS_URL + subscriptionId + '/components/' + componentId
		HttpURLConnection conn = getChargifyConnection(url,GET)
		conn.connect()
		int responseCode = conn.getResponseCode()
		//check response code
		def content = []
		if (responseCode == HTTP_RESPONSE_CODE_OK){
			content = JSON.parse(conn.content?.text)
		}
		//Todo add error handling
		return content
	}


	def updateMeteredUsage(subscriptionId, componentId, amount, description) {
		def url = SUBSCRIPTIONS_URL + subscriptionId + '/components/' + componentId + '/usages.json'
		HttpURLConnection conn = getChargifyConnection(url,POST)
		conn.setRequestProperty('Content-Type','application/json')
		conn.connect()

		def json = new JsonBuilder()

		json.usage {
			quantity amount
			memo 	 description
		}

		OutputStream os = conn.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
		osw.write(json.toString());
		osw.flush();
		osw.close();

		//conn.connect()
		int responseCode = conn.getResponseCode()
		//check response code
		def responseMsg = conn.responseMessage
		def errorStream = conn.getErrorStream()

		//check response code
		def content = []
		if (responseCode == HTTP_RESPONSE_CODE_OK){
			content = JSON.parse(conn.content?.text)
		}
		//Todo add error handling
		return content
	}


	def updateSubscription(subscriptionId, productHandle){
		def url = SUBSCRIPTIONS_URL + subscriptionId + ".json"
		HttpURLConnection conn = getChargifyConnection(url, PUT)
		conn.setRequestProperty('Content-Type','application/json')
		conn.connect()

		def json = new JsonBuilder()

		json.subscription {
			product_handle productHandle
		}

		def test = json.toString()

		OutputStream os = conn.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
		osw.write(json.toString());
		osw.flush();
		osw.close();

		//conn.connect()
		int responseCode = conn.getResponseCode()
		//check response code
		def responseMsg = conn.responseMessage
		def errorStream = conn.getErrorStream()

		//check response code
		def content = []
		if (responseCode == HTTP_RESPONSE_CODE_OK){
			content = JSON.parse(conn.content?.text)
		}
		//Todo add error handling
		return content

	}

	def getStatements(subscriptionId){
		def url = SUBSCRIPTIONS_URL + subscriptionId + '/statements.json'
		HttpURLConnection conn = getChargifyConnection(url,GET)
		conn.connect()
		int responseCode = conn.getResponseCode()
		//check response code
		def content = []
		if (responseCode == HTTP_RESPONSE_CODE_OK){
			content = JSON.parse(conn.content?.text)
		}
		//Todo add error handling
		return content
	}

	def getProductFamilies(){
		def url = PRODUCT_FAMILY_URL +'.json'
		HttpURLConnection conn = getChargifyConnection(url,GET)
		conn.connect()
		int responseCode = conn.getResponseCode()
		//check response code
		def content = []
		if (responseCode == HTTP_RESPONSE_CODE_OK){
			content = JSON.parse(conn.content?.text)
		}
		//Todo add error handling
		return content
	}

	def getProductFamilyComponents(productFamilyId){
		def url = PRODUCT_FAMILY_URL + '/' + productFamilyId +'/components'
		HttpURLConnection conn = getChargifyConnection(url,GET)
		conn.connect()
		int responseCode = conn.getResponseCode()
		//check response code
		def content = []
		if (responseCode == HTTP_RESPONSE_CODE_OK){
			content = JSON.parse(conn.content?.text)
		}
		//Todo add error handling
		return content
	}

	def getCall(callId){
		def url = CALL_URL + callId
		HttpURLConnection conn = getChargifyV2Connection(url,GET)
		conn.connect()
		int responseCode = conn.getResponseCode()
		//check response code
		def content = []
		if (responseCode == HTTP_RESPONSE_CODE_OK){
			content = JSON.parse(conn.content?.text)
		}
		//Todo add error handling
		return content
	}

	//https://labs.hybris.com/2012/09/17/hmac-sha1-in-groovy/
	def hmac(String data, String key) throws SignatureException {
		String result
		try {
			// get an hmac_sha1 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA1")
			// get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance("HmacSHA1")
			mac.init(signingKey)
			// compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(data.getBytes())
			result= rawHmac.encodeHex()
		}
		catch (Exception e) {
			throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
		}
		return result
	}
}
