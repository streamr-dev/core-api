package com.unifina

class UnifinaSecurityService {
	
	def springSecurityService
	
	boolean checkUser(instance) {
		if (instance.hasProperty("user") && instance.user?.id!=null)
			return instance.user.id == springSecurityService.getCurrentUser().id
		else return true
	}
	
	boolean canAccess(instance) {
		if (instance) {
			if (!checkUser(instance)) {
				return false
			}
		}
		return true
	}
	
}
