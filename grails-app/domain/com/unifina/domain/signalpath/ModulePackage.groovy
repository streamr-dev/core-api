package com.unifina.domain.signalpath

import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser

class ModulePackage {
	String name
	SecUser user

	static hasMany = [permissions: Permission]
}
