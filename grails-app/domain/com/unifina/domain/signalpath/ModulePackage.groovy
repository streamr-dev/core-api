package com.unifina.domain.signalpath

import com.unifina.domain.security.Permission

class ModulePackage {
	String name

	static hasMany = [permissions: Permission]
}
