package com.unifina.domain.signalpath

import com.unifina.domain.security.SecUser
import com.unifina.utils.IdGenerator
import grails.converters.JSON

class Canvas {

	enum Type {
		TEMPLATE(0),
		EXAMPLE(1),
		RUNNING(2)

		Integer id

		Type(Integer id) {
			this.id = id
		}
	}

	enum State {
		STARTING("starting"),
		RUNNING("running"),
		STOPPED("stopped")

		String id

		State(String id) {
			this.id = id
		}
	}

	String id
	SecUser user
	Date dateCreated
	Date lastUpdated

	String name
	String json
	Type type

	// SavedSignalPath
	Boolean hasExports = false

	// RunningSignalPath
	String runner
	String server
	String requestUrl
	Boolean shared
	Boolean adhoc
	State state
	String serialized
	Date serializationTime

	static constraints = {
		runner(nullable: true)
		server(nullable: true)
		requestUrl(nullable: true)
		shared(nullable: true)
		adhoc(nullable: true)
		state(nullable: true)
		serialized(nullable: true)
		serializationTime(nullable: true)
	}

	static mapping = {
		id generator: IdGenerator.name // Note: doesn't apply in unit tests
		json type: 'text'
		hasExports defaultValue: false
		runner index: 'runner_idx'
		uiChannels cascade: 'all-delete-orphan'
		serialized type: 'text'
	}

	static hasMany = [uiChannels: UiChannel]

	boolean isNotSerialized() {
		serialized == null || serialized.empty
	}

	def toMap() {
		Map map = JSON.parse(json)
		return [
			id: id,
			name: name,
			modules: map?.modules,
			settings: map?.settings,
			hasExports: map?.hasExports,
		]
	}
}
