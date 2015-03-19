package com.unifina.service

import groovy.transform.CompileStatic

import org.apache.log4j.Logger

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.domain.signalpath.ModulePackageUser
import com.unifina.domain.signalpath.RunningSignalPath

class UnifinaSecurityService {
	
	def springSecurityService
	Logger log = Logger.getLogger(UnifinaSecurityService)
	
	/**
	 * Checks if the given user has access to the given instance.
	 * If no user is provided, the current logged in user (as returned by springSecurityService.currentUser) is used.
	 * 
	 * Access to instance is granted if the instance has a field called "user",
	 * and the user id it points to equals the user id being checked against.
	 * @param instance
	 * @param user
	 * @return
	 */
	private boolean checkUser(instance, SecUser user=null) {
		if (user==null)
			user = springSecurityService.getCurrentUser()
		
		if (instance.hasProperty("user") && instance.user?.id!=null) {
			boolean result = instance.user.id == user?.id
			if (!result) {
				log.warn("User ${user?.id} tried to access $instance owned by user $instance.user.id!")
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
	
	@CompileStatic 
	boolean canAccess(RunningSignalPath rsp, String apiKey=null) {
		return rsp.shared || checkUser(rsp) || checkApiKey(rsp, apiKey)
	}
	
	private boolean checkModulePackageAccess(ModulePackage modulePackage) {
		ModulePackageUser mpu = ModulePackageUser.findByUserAndModulePackage(springSecurityService.currentUser, modulePackage)
		return mpu != null
	}
	
	/**
	 * Checks if the given API key should grant access to the given protected instance
	 * @param username
	 * @param dataToken
	 * @return
	 */
	boolean checkApiKey(Object instance, String apiKey) {
		SecUser user = SecUser.findByApiKey(apiKey)
		if (!apiKey)
			return false
		else return checkUser(instance, user)
	}

}
