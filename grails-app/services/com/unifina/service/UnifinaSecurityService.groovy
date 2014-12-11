package com.unifina.service

import grails.plugin.springsecurity.SpringSecurityService
import groovy.transform.CompileStatic

import org.apache.log4j.Logger

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.domain.signalpath.ModulePackageUser

import java.util.UUID

class UnifinaSecurityService {
	
	def springSecurityService
	Logger log = Logger.getLogger(UnifinaSecurityService)
	
	private boolean checkUser(instance) {
		if (instance.hasProperty("user") && instance.user?.id!=null) {
			boolean result = instance.user.id == springSecurityService.getCurrentUser()?.id
			if (!result) {
				log.warn("User ${springSecurityService.currentUser?.id} tried to access $instance owned by user $instance.user.id!")
				return false
			}
			return result
		}
		else return true
	}

	@CompileStatic
	boolean canAccess(Object instance) {
		if (instance) {
			if (!checkUser(instance)) {
				return false
			}
		}
		return true
	}

	@CompileStatic
	boolean canAccess(Module module) {
		return canAccess(module.modulePackage)
	}
	
	@CompileStatic
	boolean canAccess(ModulePackage modulePackage) {
		return checkModulePackageAccess(modulePackage) || checkUser(modulePackage)
	}
	
	private boolean checkModulePackageAccess(ModulePackage modulePackage) {
		ModulePackageUser mpu = ModulePackageUser.findByUserAndModulePackage(springSecurityService.currentUser, modulePackage)
		return mpu != null
	}

	SecUser checkDataToken(String username, String dataToken) {
		SecUser user = SecUser.findByUsernameAndDataToken(username,dataToken)
		if (!user)
			throw new RuntimeException("Invalid username or token")
		else return user
	}

	String createDataToken() {
		return UUID.randomUUID();
	}

}
