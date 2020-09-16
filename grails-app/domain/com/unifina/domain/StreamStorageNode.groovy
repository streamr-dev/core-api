package com.unifina.domain

import grails.persistence.Entity
import groovy.transform.EqualsAndHashCode

@Entity
@EqualsAndHashCode(includes="streamId,storageNodeAddress")
class StreamStorageNode implements Serializable {
	String streamId
	String storageNodeAddress
	Date dateCreated

	static constraints = {
		streamId(nullable: false)
		storageNodeAddress(nullable: false, validator: EthereumAddressValidator.validate)
	}

	static mapping = {
		id composite: [
			'streamId',
			'storageNodeAddress',
		]
	}

	Map<String,? extends Object> toMap() {
		return [
			streamId: streamId,
			storageNodeAddress: storageNodeAddress,
			dateCreated: dateCreated
		]
	}
}
