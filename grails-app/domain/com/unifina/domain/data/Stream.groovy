package com.unifina.domain.data


import com.unifina.domain.ExampleType
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.Permission
import com.unifina.domain.signalpath.Canvas
import grails.converters.JSON
import groovy.transform.CompileStatic

class Stream implements Comparable {
	public final static Integer DEFAULT_PARTITIONS = 1
	public final static String DEFAULT_NAME = "Untitled Stream"
	public final static boolean DEFAULT_UI_CHANNEL = false
	public final static boolean DEFAULT_INBOX = false
	public final static boolean DEFAULT_REQUIRE_SIGNED_DATA = false
	public final static boolean DEFAULT_REQUIRE_ENCRYPTED_DATA = false
	public final static boolean DEFAULT_AUTO_CONFIGURE = true
	public final static Integer DEFAULT_STORAGE_DAYS = 365
	public final static Integer DEFAULT_INACTIVITY_THRESHOLD_HOURS = 48
	public final static ExampleType DEFAULT_EXAMPLE_TYPE = ExampleType.NOT_SET

	String id
	Integer partitions = DEFAULT_PARTITIONS

	String name = DEFAULT_NAME
	String config
	String description

	Date firstHistoricalDay
	Date lastHistoricalDay

	Date dateCreated
	Date lastUpdated

	Boolean uiChannel = DEFAULT_UI_CHANNEL
	String uiChannelPath
	Canvas uiChannelCanvas

	Boolean inbox = DEFAULT_INBOX

	Boolean requireSignedData = DEFAULT_REQUIRE_SIGNED_DATA
	// Stream requires data to be encrypted
	Boolean requireEncryptedData = DEFAULT_REQUIRE_ENCRYPTED_DATA
	// Always try to autoconfigure field names and types
	Boolean autoConfigure = DEFAULT_AUTO_CONFIGURE
	// Historical data storage period (days)
	Integer storageDays = DEFAULT_STORAGE_DAYS
	// inactivityThresholdHours is the inactivity period for a stream in hours
	Integer inactivityThresholdHours = DEFAULT_INACTIVITY_THRESHOLD_HOURS
	// exampleType marks this Stream as an example for new users.
	ExampleType exampleType = DEFAULT_EXAMPLE_TYPE

	static hasMany = [
		permissions: Permission,
		products: Product
	]
	static mappedBy = [products: 'streams'] // defines which field in Product maps back to Streams - need to define this explicitly because there is also previewStream
	static belongsTo = Product

	static constraints = {
		partitions(nullable: false, min: 1)
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
}
