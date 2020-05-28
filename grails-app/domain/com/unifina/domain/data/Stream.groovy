package com.unifina.domain.data

import com.unifina.domain.ExampleType
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Permission
import com.unifina.domain.signalpath.Canvas
import grails.converters.JSON
import grails.persistence.Entity
import groovy.transform.CompileStatic

@Entity
class Stream implements Comparable {
	public final static String DEFAULT_NAME = "Untitled Stream"
	public final static Integer DEFAULT_STORAGE_DAYS = 365
	public final static Integer DEFAULT_INACTIVITY_THRESHOLD_HOURS = 48
	String id
	Integer partitions = 1

	String name = DEFAULT_NAME
	String config
	String description

	Date firstHistoricalDay
	Date lastHistoricalDay

	Date dateCreated
	Date lastUpdated

	Boolean uiChannel = false
	String uiChannelPath
	Canvas uiChannelCanvas

	Boolean inbox = false

	Boolean requireSignedData = false
	// Stream requires data to be encrypted
	Boolean requireEncryptedData = false
	// Always try to autoconfigure field names and types
	Boolean autoConfigure = true
	// Historical data storage period (days)
	Integer storageDays = DEFAULT_STORAGE_DAYS
	// inactivityThresholdHours is the inactivity period for a stream in hours
	Integer inactivityThresholdHours = DEFAULT_INACTIVITY_THRESHOLD_HOURS
	// exampleType marks this Stream as an example for new users.
	ExampleType exampleType = ExampleType.NOT_SET

	static hasMany = [
		permissions: Permission,
		products: Product
	]
	static mappedBy = [products: 'streams'] // defines which field in Product maps back to Streams - need to define this explicitly because there is also previewStream
	static belongsTo = Product

	static constraints = {
		partitions(nullable: false, min: 1, max: 100)
		name(blank:false)
		config(nullable:true)
		description(nullable:true)
		firstHistoricalDay(nullable:true)
		lastHistoricalDay(nullable:true)
		uiChannelPath(nullable:true)
		uiChannelCanvas(nullable:true)
		autoConfigure(nullable: false)
		storageDays(nullable: false, min: 0)
		inactivityThresholdHours(nullable: false, min: 0)
	}

	static mapping = {
		id generator: 'assigned'
		name index: "name_idx"
		uiChannel defaultValue: "false"
		uiChannelPath index: "ui_channel_path_idx"
		config type: 'text'
		inbox defaultValue: "false"
		requireSignedData defaultValue: "false"
		requireEncryptedData defaultValue: "false"
		autoConfigure defaultValue: "true"
		storageDays defaultValue: DEFAULT_STORAGE_DAYS
		exampleType enumType: "identity", defaultValue: ExampleType.NOT_SET, index: 'example_type_idx'
	}

	@Override
	String toString() {
		return name
	}

	// TODO: in PR phase, coordinate with frontend to remove dependency on stream.feed.* (maybe used in Editor)
	@CompileStatic
	Map toMap() {
		[
			id: id,
			partitions: partitions,
			name: name,
			config: config == null || config.empty ? config : JSON.parse(config),
			description: description,
			uiChannel: uiChannel,
			inbox: inbox,
			dateCreated: dateCreated,
			lastUpdated: lastUpdated,
			requireSignedData: requireSignedData,
			requireEncryptedData: requireEncryptedData,
			autoConfigure: autoConfigure,
			storageDays: storageDays,
			inactivityThresholdHours: inactivityThresholdHours,
		]
	}

	@CompileStatic
	Map toSummaryMap() {
		[
			id: id,
			partitions: partitions,
			name: name,
			description: description,
			uiChannel: uiChannel,
			inbox: inbox,
			dateCreated: dateCreated,
			lastUpdated: lastUpdated,
			requireSignedData: requireSignedData,
			requireEncryptedData: requireEncryptedData,
		]
	}

	/**
	 * Returns only the public fields required for validating messages in streams
	 */
	@CompileStatic
	Map toValidationMap() {
		[
			id: id,
			partitions: partitions,
			requireSignedData: requireSignedData,
			requireEncryptedData: requireEncryptedData,
		]
	}

	@Override
	int compareTo(Object arg0) {
		if (!(arg0 instanceof Stream)) return 0
		else return arg0.name.compareTo(this.name)
	}

	@Override
	int hashCode() {
		return id.hashCode()
	}

	@Override
	boolean equals(Object obj) {
		return obj instanceof Stream && obj.id == this.id
	}

	Map<String, Object> getStreamConfigAsMap() {
		if (config!=null)
			return ((Map)JSON.parse(config));
		else return [:]
	}

	boolean isStale(Date now, Date latestDataTimestamp) {
		if (inactivityThresholdHours == 0) {
			return false
		} else {
			Calendar calendar = Calendar.getInstance()
			calendar.setTime(now)
			calendar.add(Calendar.HOUR_OF_DAY, -inactivityThresholdHours)
			Date threshold = calendar.getTime()

			return latestDataTimestamp.before(threshold)
		}
	}

	/**
	 * Return true if this Stream is an UI Channel.
	 */
	@CompileStatic
	boolean isUIChannel() {
		return uiChannel && uiChannelCanvas != null && uiChannelPath != null
	}

	/**
	 * Return module id parsed from uiChannelPath or throw an IllegalArgumentException on error.
	 */
	@CompileStatic
	Integer parseModuleID() {
		if (uiChannelPath == null) {
			throw new IllegalArgumentException("Unable to parse module if from null uiChannelPath.")
		}
		String[] components = uiChannelPath.split("/")
		if (components.length >= 5 ) {
			try {
				return Integer.parseInt(components[4])
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Unable to parse module id from '" + components[4] + "'.")
			}
		}
		throw new IllegalArgumentException("UI Channel path doesn't contain module id.")
	}
}
