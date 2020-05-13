package com.unifina.domain.signalpath

import com.unifina.domain.security.Permission
import grails.persistence.Entity

@Entity
class ModulePackage {
	String name

	static hasMany = [permissions: Permission]
}
