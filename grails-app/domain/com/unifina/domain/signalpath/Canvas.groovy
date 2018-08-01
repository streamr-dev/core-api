package com.unifina.domain.signalpath

import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.security.Permission
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

	Serialization serialization
	// startedBy is set to user who started the canvas.
	SecUser startedBy

	static hasMany = [
		dashboardItems: DashboardItem,
		permissions: Permission
	]

	static constraints = {
		runner(nullable: true)
		server(nullable: true)
		requestUrl(nullable: true)
		serialization(nullable: true, unique: true)
		startedBy(nullable: true)
	}

	static mapping = {
		id generator: IdGenerator.name // Note: doesn't apply in unit tests
		json type: 'text'
		hasExports defaultValue: false
		example defaultValue: false
		adhoc defaultValue: false
		runner index: 'runner_idx'
		dashboardItems cascade: 'all-delete-orphan'
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
			serialized: serialization != null,
			modules: map?.modules,
			settings: map?.settings,
			uiChannel: map?.uiChannel,
		]
	}
}
