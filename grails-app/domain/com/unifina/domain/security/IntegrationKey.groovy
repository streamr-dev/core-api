package com.unifina.domain.security

import com.unifina.security.StringEncryptor
import com.unifina.utils.IdGenerator
import grails.converters.JSON
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.util.Assert

import javax.annotation.PostConstruct

class IntegrationKey implements Serializable {
	def transient grailsApplication
	transient StringEncryptor encryptor
	String id
	SecUser user
	String name
	Service service
	String json
	String idInService

	Date dateCreated
	Date lastUpdated

	@PostConstruct
	void init() {
		String password = grailsApplication.config["streamr"]["encryption"]["password"]
		Assert.notNull(password, "streamr.encryption.password not set!")
		encryptor = new StringEncryptor(password)
	}

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
				String decryptedPrivateKey = encryptor.decrypt(jso.getString("privateKey"), user.id.byteValue())
				jsmap.put("privateKey", decryptedPrivateKey)
			}
			return jsmap
		} else {
			return [:]
		}
	}

}
