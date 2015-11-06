package com.unifina.service

import com.unifina.UserCreationFailedException
import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedUser
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.domain.signalpath.ModulePackageUser

import com.unifina.domain.security.SecRole

class UserService {
    def UserRole
    def Role
	
    def createUser(Map properties, List<SecRole> roles=null, List<Feed> feeds=null, List<ModulePackage> packages=null) {
        ClassLoader cl = this.getClass().getClassLoader()
        SecUser user = cl.loadClass(grailsApplication.config.grails.plugin.springsecurity.userLookup.userDomainClassName).newInstance(properties)
        
        UserRole = lookupUserRoleClass()
        Role = lookupRoleClass()
		
        user.properties = properties
        user.name = cmd.name // not copied by above line for some reason
        user.password = springSecurityService.encodePassword(user.password)
        user.enabled = true
        user.accountLocked = false
        
        if (!user.validate()) {
            throw new UserCreationFailedException("Registration user validation failed: "+user.errors)
        }
        
        if (!user.save(flush:true)) {
            log.warn("Failed to save user data: "+user.errors)
            throw new UserCreationFailedException()
        } else {
            // Save roles, feeds and module packages
            // If list is given, use it, otherwise get the default from config
            
            for (roleName in ((roles != null) ? roles : conf.ui.register.defaultRoleNames)) {
                UserRole.create user, Role.findByAuthority(roleName)
            } 
            for (feedId in ((feeds != null) ? feeds : grailsApplication.config.streamr.user.defaultFeeds)) {
                new FeedUser(user: user, feed: Feed.load(feedId)).save(flush: true)
            }
            for (modulePackageId in ((feeds != null) ? feeds : grailsApplication.config.streamr.user.defaultModulePackages)) {
                new ModulePackageUser(user: user, modulePackage: ModulePackage.load(modulePackageId)).save(flush: true)
            }
        }
        log.info("Created user for "+user.username)
        
        return user
    }
}
