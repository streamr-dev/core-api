package com.unifina.domain.tour

import org.apache.commons.lang.builder.HashCodeBuilder

import com.unifina.domain.security.SecUser

class TourUser implements Serializable {
	SecUser user
	Integer tourNumber
	Date completedAt

	static mapping = {
		id composite: ['user', 'tourNumber']
		version false
	}
	
	boolean equals(other) {
		if (!(other instanceof TourUser)) {
			return false
		}

		other.user?.id == user?.id &&
			other.tourNumber == tourNumber
	}

	int hashCode() {
		def builder = new HashCodeBuilder()
		if (user) builder.append(user.id)
		if (tourNumber) builder.append(tourNumber)
		builder.toHashCode()
	}
}
