package com.unifina.domain.security

import com.unifina.utils.IdGenerator
import grails.converters.JSON
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.web.json.JSONObject

class IntegrationKey implements Serializable {

	String id
	SecUser user
	String name
	Service service
	String json
	String idInService

	Date dateCreated
	Date lastUpdated

	static mapping = {
		id generator: IdGenerator.name // Note: doesn't apply in unit tests
		json type: 'text'
		idInService(index: "id_in_service_and_service_idx")
		service(index: "id_in_service_and_service_idx")
	}

	static constraints = {
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
			return [address: ((JSONObject) JSON.parse(json)).get("address")]
		} else {
			return [:]
		}
	}

}
