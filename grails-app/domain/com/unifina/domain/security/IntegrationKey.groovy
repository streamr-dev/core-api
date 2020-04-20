package com.unifina.domain.security

import com.unifina.service.EthereumIntegrationKeyService
import com.unifina.utils.IdGenerator
import grails.converters.JSON
import grails.util.Holders
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.web.json.JSONObject

class IntegrationKey implements Serializable {
	//private transient EthereumIntegrationKeyService ethereumIntegrationKeyService
	String id
	SecUser user
	String name
	Service service
	String json
	String idInService

	Date dateCreated
	Date lastUpdated
/*
	EthereumIntegrationKeyService getEthereumIntegrationKeyService(){
		if(ethereumIntegrationKeyService == null)
			ethereumIntegrationKeyService = Holders.getApplicationContext().getBean(EthereumIntegrationKeyService.class)
		return ethereumIntegrationKeyService
	}
*/
	static mapping = {
		id generator: IdGenerator.name // Note: doesn't apply in unit tests
		json type: 'text'
		user lazy: false
		idInService(index: "id_in_service_and_service_idx")
		service(index: "id_in_service_and_service_idx")
	}

	static constraints = {
		idInService unique: true
	}

	enum Service {
		ETHEREUM,
		ETHEREUM_ID
	}

	@CompileStatic
	Map toMap() {
		return [
				id  : id,
				user: user.id,
				name: name,
				service: service.toString(),
				json: jsonMap()
		]
	}

	@CompileStatic
	private Map jsonMap() {
		if (service == Service.ETHEREUM || service == Service.ETHEREUM_ID) {
			JSONObject jso = (JSONObject) JSON.parse(json)
			Map jsmap = [address: jso.get("address")]
			if(service == Service.ETHEREUM) {
				//String decryptedPrivateKey = getEthereumIntegrationKeyService().decryptPrivateKey(this)
				String decryptedPrivateKey = Holders.getApplicationContext().getBean(EthereumIntegrationKeyService.class).decryptPrivateKey(this)
				jsmap.put("privateKey", decryptedPrivateKey)
			}
			return jsmap
		} else {
			return [:]
		}
	}

}
