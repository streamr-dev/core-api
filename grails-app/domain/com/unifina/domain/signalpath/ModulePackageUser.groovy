package com.unifina.domain.signalpath

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder

import com.unifina.domain.security.SecUser;

class ModulePackageUser implements Serializable {
	SecUser user
	ModulePackage modulePackage
	
	static mapping = {
		id composite: ['user', 'modulePackage']
		version false
	}
	
	boolean equals(other) {
		if (!(other instanceof ModulePackageUser)) {
			return false
		}

		other.user?.id == user?.id &&
			other.modulePackage?.id == modulePackage?.id
	}

	int hashCode() {
		def builder = new HashCodeBuilder()
		if (user) builder.append(user.id)
		if (modulePackage) builder.append(modulePackage.id)
		builder.toHashCode()
	}
}
