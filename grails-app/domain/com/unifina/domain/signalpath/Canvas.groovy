package com.unifina.domain.signalpath

import com.unifina.domain.ExampleType
import com.unifina.domain.dashboard.DashboardItem
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.utils.IdGenerator
import grails.converters.JSON
import grails.persistence.Entity
import groovy.transform.CompileStatic

@Entity
class Canvas {
	public final static String DEFAULT_NAME = "Untitled Canvas"
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

	String name = DEFAULT_NAME
	String json = "{}"
	State state = State.STOPPED

	Boolean hasExports = false
	Boolean adhoc = false

	String runner
	String server
	String requestUrl

	Serialization serialization
	// startedBy is set to user who started the canvas.
	SecUser startedBy

	// exampleType marks this Canvas as an example for new users.
	ExampleType exampleType = ExampleType.NOT_SET

	static hasMany = [
		dashboardItems: DashboardItem,
		permissions: Permission
	]

	static constraints = {
		name(blank: false)
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
		adhoc defaultValue: false
		runner index: 'runner_idx'
		dashboardItems cascade: 'all-delete-orphan'
		exampleType enumType: "identity", defaultValue: ExampleType.NOT_SET, index: 'example_type_idx'
	}

	@CompileStatic
	Map toMap() {
		Map map = (Map) JSON.parse(json)
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
			startedById: startedBy?.id,
		]
	}

	/**
	 * Returns a Map representation of this Canvas to be used in SignalPath#configure(map)
	 */
	@CompileStatic
	Map toSignalPathConfig() {
		Map map = (Map) JSON.parse(json)
		map.canvasId = id
		map.name = name
		return map
	}

	@Override
	String toString() {
		return String.format("Canvas{id=%s}", id)
	}
}
