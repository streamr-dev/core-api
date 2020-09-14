package com.unifina.domain

import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import grails.persistence.Entity
import com.unifina.utils.EthereumAddressValidator

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