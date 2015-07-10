package com.unifina.service

import groovy.transform.CompileStatic

import org.apache.log4j.Logger

import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.domain.signalpath.ModulePackageUser
import com.unifina.domain.signalpath.RunningSignalPath
import com.unifina.domain.signalpath.SavedSignalPath

class UnifinaSecurityService {
	
	def springSecurityService
	Logger log = Logger.getLogger(UnifinaSecurityService)
	
	/**
	 * Checks if the given user has access to the given instance.
	 * If no user is provided, the user identified by api keys or the current logged in user (as returned by springSecurityService.currentUser) is used
	 * (in that order of precedence).
	 * 
	 * Access to instance is granted if the instance has a field called "user",
	 * and the user id it points to equals the user id being checked against.
	 * 
	 * @param instance
	 * @param user
	 * @return
	 */
	private boolean checkUser(instance, SecUser user=null, boolean logIfDenied=true, String apiKey=null, String apiSecret=null) {
		// What user are we authenticating?
		if (user==null) {
			// Api keys take precedence over session user
			if (apiKey!=null) {
				user = getUserByApiKey(apiKey, apiSecret)
			}
			if (user==null)
				user = springSecurityService.getCurrentUser()
		}
		
		// Is this a protected instance?
		if (instance.hasProperty("user") && instance.user?.id!=null) {
			boolean result = instance.user.id == user?.id
			if (!result && logIfDenied) {
				log.warn("User ${user?.id} tried to access $instance owned by user $instance.user.id!")
			}
			return result
		}
		// For unprotected instances, return true
		else return true
	}

	@CompileStatic
	boolean canAccess(Object instance, String apiKey=null, String apiSecret=null) {
		if (instance) {
			
			if (!checkUser(instance, null, true, apiKey, apiSecret)) {
				return false
			}
		}
		// TODO: questionable
		return true
	}

	@CompileStatic
	boolean canAccess(Module module) {
		return canAccess(module.modulePackage)
	}
	
	@CompileStatic
	boolean canAccess(ModulePackage modulePackage) {
		// Everyone who has been granted access to a ModulePackage can access it
		return checkModulePackageAccess(modulePackage) || checkUser(modulePackage)
	}
	
	@CompileStatic 
	boolean canAccess(RunningSignalPath rsp, String apiKey=null, String apiSecret=null) {
		// Shared RunningSignalPaths can be accessed by everyone
		return rsp.shared || checkUser(rsp, null, true, apiKey, apiSecret)
	}
	
	@CompileStatic
	boolean canAccess(SavedSignalPath ssp, boolean isLoad) {
		// Examples can be read by everyone
		if (isLoad && ssp.type==SavedSignalPath.TYPE_EXAMPLE_SIGNAL_PATH)
			return true
		else return canAccess(ssp)
	}
	
	private boolean checkModulePackageAccess(ModulePackage modulePackage) {
		ModulePackageUser mpu = ModulePackageUser.findByUserAndModulePackage(springSecurityService.currentUser, modulePackage)
		return mpu != null
	}
	
	/**
	 * Looks up a user based on api keys. Returns null if the keys do not match a user.
	 * @param apiKey
	 * @param apiSecret
	 * @return
	 */
	SecUser getUserByApiKey(String apiKey, String apiSecret) {
		if (!apiKey || !apiSecret)
			return null
			
		SecUser user = SecUser.findByApiKey(apiKey)
		if (!user || user.apiSecret != apiSecret)
			return null
		else return user
	}

}
