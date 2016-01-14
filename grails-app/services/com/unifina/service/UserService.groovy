package com.unifina.service

import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedUser
import com.unifina.domain.security.SecRole
import com.unifina.domain.security.SecUser
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.domain.signalpath.ModulePackageUser
import com.unifina.user.UserCreationFailedException

class UserService {
    
	def grailsApplication
    def springSecurityService
    def permissionService
	
    def createUser(Map properties, List<SecRole> roles=null, List<Feed> feeds=null, List<ModulePackage> packages=null) {
        def secConf = grailsApplication.config.grails.plugin.springsecurity
        ClassLoader cl = this.getClass().getClassLoader()
        SecUser user = cl.loadClass(secConf.userLookup.userDomainClassName).newInstance(properties)

        // Encode the password
        if(user.password == null)
            throw new UserCreationFailedException("The password is empty!")
        user.password = springSecurityService.encodePassword(user.password)

        // When created, the account is always enabled
        user.enabled = true
        
        if (!user.validate()) {
            log.warn(permissionService.checkErrors(user.errors.getAllErrors()))
            throw new UserCreationFailedException("Registration user validation failed: "+permissionService.checkErrors(user.errors.getAllErrors()))
        }

        if (!user.save(flush:true)) {
            log.warn("Failed to save user data: "+permissionService.checkErrors(user.errors.getAllErrors()))
            throw new UserCreationFailedException()
        } else {
            // Save roles, feeds and module packages
            addRoles(user, roles)
            addFeeds(user, feeds)
            addModulePackages(user, packages)
        }
        log.info("Created user for "+user.username)
        
        return user
    }

    def addRoles(user, List<SecRole> roles=null) {
        def secConf = grailsApplication.config.grails.plugin.springsecurity
        ClassLoader cl = this.getClass().getClassLoader()

        def userRoleClass = cl.loadClass(secConf.userLookup.authorityJoinClassName)
        def roleClass = cl.loadClass(secConf.authority.className)

        if(roles == null) {
            roles = roleClass.findAllByAuthorityInList(secConf.ui.register.defaultRoleNames)
            if(roles.size() != secConf.ui.register.defaultRoleNames.size())
                throw new RuntimeException("Roles not found: "+secConf.ui.register.defaultRoleNames)
        }

        roles.each { role ->
            userRoleClass.create user, role
        }
    }

    def addFeeds(user, List<Feed> feeds=null) {
        if(feeds == null) {
            feeds = Feed.findAllByIdInList(grailsApplication.config.streamr.user.defaultFeeds.collect {it.longValue()})
            if(feeds.size() != grailsApplication.config.streamr.user.defaultFeeds.size())
                throw new RuntimeException("Feeds not found: "+grailsApplication.config.streamr.user.defaultFeeds)
        }

        feeds.each { feed ->
            new FeedUser(user: user, feed: feed).save(flush: true, failOnError: true)
        }
    }

    def addModulePackages(user, List<ModulePackage> packages=null) {
        if (packages == null) {
            packages = ModulePackage.findAllByIdInList(grailsApplication.config.streamr.user.defaultModulePackages.collect {it.longValue()})
            if(packages.size() != grailsApplication.config.streamr.user.defaultModulePackages.size())
                throw new RuntimeException("ModulePackages not found: "+grailsApplication.config.streamr.user.defaultModulePackages)
        }

        packages.each { modulePackage ->
            new ModulePackageUser(user: user, modulePackage: modulePackage).save(flush: true, failOnError: true)
        }
    }
}
