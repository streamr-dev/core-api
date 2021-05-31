package com.unifina.domain

import com.unifina.utils.IdGenerator
import grails.persistence.Entity
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@Entity
@EqualsAndHashCode(includes = "id,streamId,storageNodeAddress,dateCreated")
@ToString
class StreamStorageNode {
	String id
	String streamId // TODO: remove streamId?
	String storageNodeAddress
	Date dateCreated

	static belongsTo = [
		stream: Stream,
	]

	static constraints = {
		streamId(nullable: false)
		storageNodeAddress(nullable: false, validator: EthereumAddressValidator.validate)
	}

	static mapping = {
		id(generator: IdGenerator.name)
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
