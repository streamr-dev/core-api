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

	Date dateCreated
	Date lastUpdated

	static mapping = {
		id generator: IdGenerator.name // Note: doesn't apply in unit tests
		json type: 'text'
	}

	static constraints = {
	}

	enum Service {
		ETHEREUM
	}

	@CompileStatic
	Map toMap() {
		return [
				id  : id,
				user: user.id,
				name: name,
				service: service.toString(),
				json: (JSONObject) JSON.parse(json)
		]
	}

}
