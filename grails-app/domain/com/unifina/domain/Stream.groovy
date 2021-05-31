package com.unifina.domain


import grails.converters.JSON
import grails.persistence.Entity
import groovy.transform.CompileStatic

@Entity
class Stream implements Comparable {
	public final static Integer DEFAULT_STORAGE_DAYS = 365
	public final static Integer DEFAULT_INACTIVITY_THRESHOLD_HOURS = 48

	String id
	Integer partitions = 1

	String name

	String config
	String description

	Date firstHistoricalDay
	Date lastHistoricalDay

	Date dateCreated
	Date lastUpdated

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
		products: Product,
		storageNodes: StreamStorageNode,
	]
	static mappedBy = [
		products: 'streams',
	]
	// defines which field in Product maps back to Streams - need to define this explicitly because there is also previewStream
	static belongsTo = Product

	static constraints = {
		id(nullable: false, unique: true)
		partitions(nullable: false, min: 1, max: 100)
		name(blank: false, maxSize: 255)
		config(nullable: true, maxSize: 100000)
		description(nullable: true, maxSize: 255)
		firstHistoricalDay(nullable: true)
		lastHistoricalDay(nullable: true)
		requireSignedData(nullable: false)
		requireEncryptedData(nullable: false)
		autoConfigure(nullable: false)
		storageDays(nullable: false, min: 0, max: Integer.MAX_VALUE)
		inactivityThresholdHours(nullable: false, min: 0, max: Integer.MAX_VALUE)
		exampleType(nullable: false)
	}

	static mapping = {
		id generator: 'assigned'
		name index: "name_idx"
		config type: 'text'
		requireSignedData defaultValue: "false"
		requireEncryptedData defaultValue: "false"
		autoConfigure defaultValue: "true"
		storageDays defaultValue: DEFAULT_STORAGE_DAYS
		exampleType enumType: "ordinal", defaultValue: ExampleType.NOT_SET.ordinal(), index: "example_type_idx"
		storageNodes cascade: "all-delete-orphan"
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
			dateCreated: dateCreated,
			lastUpdated: lastUpdated,
			requireSignedData: requireSignedData,
			requireEncryptedData: requireEncryptedData,
			storageDays: storageDays,
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
			storageDays: storageDays,
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

	static Map<String, Object> normalizeConfig(Map<String, Object> config) {
		if (config == null) {
			config = new HashMap<String, Object>()
		}
		if (!config.fields) {
			config.fields = []
		}
		return config
	}

	static Map<String, Object> getStreamConfigAsMap(String config) {
		if (config != null)
			return ((Map) JSON.parse(config));
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
