package com.unifina.domain.dataunion

import com.unifina.domain.marketplace.Product
import com.unifina.utils.IdGenerator
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import groovy.transform.ToString

@ToString
@Entity
class DataUnionSecret {

	String id
	// name to display for users.
	String name
	// secret that enables automatic joins to the data union.
	String secret
	// contractAddress is an Ethereum address of the data union smart contract.
	String contractAddress

    static constraints = {
		contractAddress(validator: Product.isEthereumAddress)
    }
	static mapping = {
		id generator: IdGenerator.name
	}

	@GrailsCompileStatic
	Map toMap() { [
		id: id,
		name: name,
		contractAddress: contractAddress,
		secret: secret,
	] }

}
