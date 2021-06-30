package com.unifina.domain

import com.unifina.utils.IdGenerator
import grails.converters.JSON
import grails.persistence.Entity
import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.codehaus.groovy.grails.web.json.JSONObject

@Entity
@ToString
class IntegrationKey implements Serializable {

	String id
	User user
	String name
	Service service
	String json
	String idInService

	Date dateCreated
	Date lastUpdated

	static mapping = {
		id generator: IdGenerator.name // Note: doesn't apply in unit tests
		json type: 'text'
		user lazy: false
		idInService(index: "id_in_service_and_service_idx")
		service(enumType: "string", index: "id_in_service_and_service_idx")
	}

	static constraints = {
		idInService unique: true
	}

	enum Service {
		// Ethereum Accounts on Profile page
		ETHEREUM_ID
	}

	@CompileStatic
	Map toMap() {
		return [
			id: id,
			user: user.id,
			name: name,
			service: service.toString(),
			json: jsonMap()
		]
	}

	@CompileStatic
	private Map jsonMap() {
		if (service == Service.ETHEREUM_ID) {
			return [address: ((JSONObject) JSON.parse(json)).get("address")]
		} else {
			return [:]
		}
	}
}
