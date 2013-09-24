package com.unifina

import org.apache.log4j.Logger;

class UnifinaSecurityService {
	
	def springSecurityService
	Logger log = Logger.getLogger(UnifinaSecurityService)
	
	boolean checkUser(instance) {
		if (instance.hasProperty("user") && instance.user?.id!=null) {
			boolean result = instance.user.id == springSecurityService.getCurrentUser().id
			if (!result)
				log.warn("User $springSecurityService.currentUser.id tried to access $instance owned by user $instance.user.id!")
			return result
		}
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
