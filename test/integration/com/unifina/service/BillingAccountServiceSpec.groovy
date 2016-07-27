package com.unifina.service

import spock.lang.*

/**
 *
 */
class BillingAccountServiceSpec extends Specification {

	BillingAccountService billingAccountService

    def setup() {
		billingAccountService = new BillingAccountService()
    }

    def cleanup() {
    }

    void "we should get 200 as response code when fetching products"() {
		when:
		def response = billingAccountService.getProducts()
		then:
		response.code == 200
    }

	void "we should get 200 as response code when fetching product families"() {
		when:
		def response = billingAccountService.getProductFamilies()
		then:
		response.code == 200
	}

	void "we shoud get 404 as response code when fetching customer with obviously wrong reference string"() {
		when:
		def response = billingAccountService.getCustomerByReference("obviously-wrong-reference-string")
		then:
		response.code == 404
	}

	void "we should get 404 as response code when fetching customer subsriptions with obviously wrong customer reference"() {
		when:
		def response = billingAccountService.getSubscriptionsByCustomerReference('obviously-wrong-reference-string')
		then:
		response.code == 404
	}

	void "we should get 404 as response code when trying to get statements with obviously wrong subscription id"() {
		when:
		def response = billingAccountService.getStatements(0)
		then:
		response.code == 404
	}

	void "we should get 404 as response code when trying to get product family components with obviously wrong product family id"() {
		when:
		def response = billingAccountService.getProductFamilyComponents(0)
		then:
		response.code == 404
	}

	void "we should get 404 as response code when trying to get call id with obviously wrong id"() {
		when:
		def response = billingAccountService.getCall(0)
		then:
		response.code == 404
	}



	/*void "we should get 200 as response code when signing up a new customer"() {
		def now = new Date()
		long timestamp = System.currentTimeMillis() / 1000L
		def conn = billingAccountService.getChargifyV2Connection("https://api.chargify.com/api/v2/signups", "POST")
		def data = 'redirect_uri='
		def NONCE = 'SECRET'
		def hmac = billingAccountService.hmac(billingAccountService.DIRECT_API_ID+timestamp+NONCE+data, billingAccountService.DIRECT_API_SECRET)


		Map<String,Object> params = new LinkedHashMap<>();
		params.put("secure[api_id]",billingAccountService.DIRECT_API_ID)
		params.put("secure[timestamp]", timestamp)
		params.put("secure[nonce]",NONCE)
		params.put("secure[data]",data)
		params.put("secure[signature]",hmac)
		params.put("signup[product][handle]","free")


		StringBuilder postData = new StringBuilder();
		for (Map.Entry<String,Object> param : params.entrySet()) {
			if (postData.length() != 0) postData.append('&');
			postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
			postData.append('=');
			postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
		}
		byte[] postDataBytes = postData.toString().getBytes("UTF-8");


		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
		conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		conn.setDoOutput(true);
		conn.connect()
		conn.getOutputStream().write(postDataBytes);

		def baz = conn.getErrorStream()
		def foo = conn.getInputStream()
		def bar = conn.getContent()

		when:
		true == true
		then:
		true == true

	}*/

	//todo mock sending a form with v2 api to chargify
}
