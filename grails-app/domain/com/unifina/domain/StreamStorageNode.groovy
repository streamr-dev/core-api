package com.unifina.domain

import grails.persistence.Entity
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@Entity
@EqualsAndHashCode(includes = "streamId,storageNodeAddress")
@ToString
class StreamStorageNode implements Serializable {
	String streamId
	String storageNodeAddress
	Date dateCreated

	StreamStorageNode(final Stream stream, final String storageNodeAddress) {
		Objects.requireNonNull(stream, "stream")
		this.stream = stream
		this.streamId = stream.getId()
		Objects.requireNonNull(storageNodeAddress, "storageNodeAddress")
		this.storageNodeAddress = storageNodeAddress
	}

	static belongsTo = [
		stream: Stream,
	]

	static constraints = {
		streamId(nullable: false)
		storageNodeAddress(nullable: false, validator: EthereumAddressValidator.validate)
	}

	static mapping = {
		id composite: [
			'streamId',
			'storageNodeAddress',
		]
		stream(insertable: false, updateable: false)
	}

	Map<String, ? extends Object> toMap() {
		return [
			streamId: streamId,
			storageNodeAddress: storageNodeAddress,
			dateCreated: dateCreated
		]
	}
}
