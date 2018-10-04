package com.unifina.domain.security

import com.unifina.utils.IdGenerator
import org.joda.time.DateTime

class SessionToken {
	String id
	String token
	String associatedAddress
	DateTime expiration

    static constraints = {
		id(primaryKeyName: "id")
		token(blank: false, nullable: false)
		associatedAddress(blank: false, nullable: false)
		expiration(blank: false, nullable: false)
    }
	static mapping = {
		id generator: IdGenerator.name
	}
}
