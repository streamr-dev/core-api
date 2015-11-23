package com.unifina.service

import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedUser
import com.unifina.domain.security.SecRole
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.domain.signalpath.ModulePackageUser
import com.unifina.user.UserCreationFailedException
import grails.plugin.springsecurity.SpringSecurityUtils

class UserService {
    
	def grailsApplication
    def springSecurityService
    def unifinaSecurityService
	
    def createUser(Map properties, List<SecRole> roles=null, List<Feed> feeds=null, List<ModulePackage> packages=null) {
        def conf = SpringSecurityUtils.securityConfig
        ClassLoader cl = this.getClass().getClassLoader()
        SecUser user = cl.loadClass(conf.userLookup.userDomainClassName).newInstance(properties)

        def userRoleClass = cl.loadClass(conf.userLookup.authorityJoinClassName)
        def roleClass = cl.loadClass(conf.authority.className)

        // Encode the password
        if(user.password == null)
            throw new UserCreationFailedException("The password is empty!")
        user.password = springSecurityService.encodePassword(user.password)

        // When created, the account is always enabled
        user.enabled = true
        
        if (!user.validate()) {
            log.warn(unifinaSecurityService.checkErrors(user.errors.getAllErrors()))
            throw new UserCreationFailedException("Registration user validation failed: "+unifinaSecurityService.checkErrors(user.errors.getAllErrors()))
        }

        // If lists are given, use them, otherwise get the defaults from config
        if(feeds == null) {
            feeds = Feed.findAllByIdInList(grailsApplication.config.streamr.user.defaultFeeds.collect {it.longValue()})
            if(feeds.size() != grailsApplication.config.streamr.user.defaultFeeds.size())
                throw new RuntimeException("Feeds not found: "+grailsApplication.config.streamr.user.defaultFeeds)
        }
        if(roles == null) {
            roles = roleClass.findAllByAuthorityInList(conf.ui.register.defaultRoleNames)
            if(roles.size() != conf.ui.register.defaultRoleNames.size())
                throw new RuntimeException("Roles not found: "+conf.ui.register.defaultRoleNames)
        }
        if(packages == null) {
            packages = ModulePackage.findAllByIdInList(grailsApplication.config.streamr.user.defaultModulePackages.collect {it.longValue()})
            if(packages.size() != grailsApplication.config.streamr.user.defaultModulePackages.size())
                throw new RuntimeException("ModulePackages not found: "+grailsApplication.config.streamr.user.defaultModulePackages)
        }
        
        if (!user.save(flush:true)) {
            log.warn("Failed to save user data: "+unifinaSecurityService.checkErrors(user.errors.getAllErrors()))
            throw new UserCreationFailedException()
        } else {
            // Save roles, feeds and module packages

            roles.each { role ->
                userRoleClass.create user, role
            }
            feeds.each { feed ->
                new FeedUser(user: user, feed: feed).save(flush: true, failOnError: true)
            }
            packages.each { modulePackage ->
                new ModulePackageUser(user: user, modulePackage: modulePackage).save(flush: true, failOnError: true)
            }
        }
        log.info("Created user for "+user.username)
        
        return user
    }
}
