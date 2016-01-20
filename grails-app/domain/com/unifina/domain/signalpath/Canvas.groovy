package com.unifina.domain.signalpath

import com.unifina.domain.security.SecUser
import com.unifina.utils.IdGenerator
import grails.converters.JSON

class Canvas {

	enum State {
		STOPPED("stopped"),
		RUNNING("running")

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
	State state

	// SavedSignalPath
	Boolean hasExports = false
	Boolean example = false

	// RunningSignalPath
	String runner
	String server
	String requestUrl
	Boolean shared
	Boolean adhoc
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
		example defaultValue: false
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
			created: dateCreated,
			updated: lastUpdated,
			adhoc: adhoc,
			state: state.toString(),
			serialized: !isNotSerialized(),
			modules: map?.modules,
			settings: map?.settings,
			uiChannel: map?.uiChannel,
			hasExports: map?.hasExports,
		]
	}
}
