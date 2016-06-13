package com.unifina.domain.signalpath

import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.security.SecUser
import com.unifina.utils.IdGenerator
import grails.converters.JSON
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.web.json.JSONObject

class Canvas {

	enum State {
		STOPPED("stopped"),
		RUNNING("running")

		String id

		State(String id) {
			this.id = id
		}

		static State fromValue(String value) {
			if (value == null) {
				return null
			} else {
				valueOf(value.toUpperCase())
			}
		}
	}

	String id
	SecUser user
	Date dateCreated
	Date lastUpdated

	String name
	String json
	State state = State.STOPPED

	Boolean hasExports = false
	Boolean example = false
	Boolean adhoc = false

	String runner
	String server
	String requestUrl
	byte[] serialized
	Date serializationTime
	static hasMany = [dashboardItems: DashboardItem]

	static constraints = {
		runner(nullable: true)
		server(nullable: true)
		requestUrl(nullable: true)
		serialized(nullable: true)
		serializationTime(nullable: true)
	}

	static mapping = {
		id generator: IdGenerator.name // Note: doesn't apply in unit tests
		json type: 'text'
		hasExports defaultValue: false
		example defaultValue: false
		adhoc defaultValue: false
		runner index: 'runner_idx'
		serialized sqlType: "mediumblob"
		dashboardItems cascade: 'all-delete-orphan'
	}

	boolean isNotSerialized() {
		serialized == null
	}

	@CompileStatic
	Map toMap() {
		Map map = (JSONObject) JSON.parse(json)
		return [
			id: id,
			name: name,
			created: dateCreated,
			updated: lastUpdated,
			adhoc: adhoc,
			state: state.toString(),
			hasExports: hasExports,
			serialized: !isNotSerialized(),
			modules: map?.modules,
			settings: map?.settings,
			uiChannel: map?.uiChannel,
		]
	}
}
